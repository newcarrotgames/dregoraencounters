package ai.torchlite.randomencounters.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;
import ai.torchlite.randomencounters.config.json.EncounterConfig;

import java.util.List;

public class AIMoveToRoute extends EntityAIBase {
    
    private final EntityCreature mob;
    private final List<EncounterConfig.Route.Waypoint> waypoints;
    private final double speed;
    private final double arriveRadius;
    private final int maxTicks;
    private final boolean loop;
    private final BlockPos origin;
    
    private int currentWaypointIndex = 0;
    private int ticks = 0;
    private int repathDelay = 0;
    private boolean completed = false;
    
    public AIMoveToRoute(EntityCreature mob, EncounterConfig.Route route, double speed, int maxTicks, BlockPos origin) {
        this.mob = mob;
        this.waypoints = route.waypoints;
        this.speed = speed;
        this.arriveRadius = route.arriveRadius;
        this.maxTicks = maxTicks;
        this.loop = "loop".equals(route.mode);
        this.origin = origin;
        
        setMutexBits(1); // MOVE flag
    }
    
    @Override
    public boolean shouldExecute() {
        if (mob.isBeingRidden() || mob.getAttackTarget() != null) return false;
        return !completed && waypoints != null && !waypoints.isEmpty();
    }
    
    @Override
    public boolean shouldContinueExecuting() {
        if (ticks >= maxTicks) return false;
        if (mob.getAttackTarget() != null) return false;
        return !completed;
    }
    
    @Override
    public void startExecuting() {
        ticks = 0;
        repathDelay = 0;
        currentWaypointIndex = 0;
        completed = false;
        tryPath();
    }
    
    @Override
    public void resetTask() {
        mob.getNavigator().clearPathEntity();
    }
    
    @Override
    public void updateTask() {
        ticks++;
        
        // Check if we've arrived at current waypoint
        BlockPos currentTarget = getCurrentWaypointPos();
        if (currentTarget != null && mob.getDistanceSqToCenter(currentTarget) <= arriveRadius * arriveRadius) {
            // Move to next waypoint
            currentWaypointIndex++;
            
            if (currentWaypointIndex >= waypoints.size()) {
                if (loop) {
                    currentWaypointIndex = 0; // Start over
                } else {
                    completed = true; // Route complete
                    return;
                }
            }
        }
        
        // Re-path periodically
        if (repathDelay-- <= 0) {
            repathDelay = 10 + mob.getRNG().nextInt(10); // re-path every ~0.5s
            tryPath();
        }
    }
    
    private void tryPath() {
        BlockPos targetPos = getCurrentWaypointPos();
        if (targetPos == null) {
            return;
        }
        
        double tx = targetPos.getX() + 0.5;
        double tz = targetPos.getZ() + 0.5;
        
        // Choose a reasonable Y near the surface
        int ty = mob.world.getHeight(targetPos).getY();
        mob.getNavigator().tryMoveToXYZ(tx, ty, tz, speed);
    }
    
    private BlockPos getCurrentWaypointPos() {
        if (waypoints == null || currentWaypointIndex >= waypoints.size()) {
            return null;
        }
        
        EncounterConfig.Route.Waypoint waypoint = waypoints.get(currentWaypointIndex);
        
        // Calculate position based on waypoint type
        int x, y, z;
        
        if (waypoint.x != null && waypoint.y != null && waypoint.z != null) {
            // Absolute position
            x = waypoint.x;
            y = waypoint.y;
            z = waypoint.z;
        } else {
            // Relative to origin
            x = origin.getX() + (waypoint.dx != null ? waypoint.dx : 0);
            y = origin.getY() + (waypoint.dy != null ? waypoint.dy : 0);
            z = origin.getZ() + (waypoint.dz != null ? waypoint.dz : 0);
        }
        
        // Handle Y mode
        if ("surface".equals(waypoint.yMode)) {
            y = mob.world.getHeight(x, z);
        }
        
        return new BlockPos(x, y, z);
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public int getCurrentWaypointIndex() {
        return currentWaypointIndex;
    }
}
