vessel.bioGraph
package viridian.content;

import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.util.Time;

import mindustry.Vars;
import mindustry.gen.Building;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BioEnergyGraph {

    private static final ArrayList<ViridianBlocks.VesselBuild> registered =
        new ArrayList<>();

    public final ArrayList<ViridianBlocks.VesselBuild> vessels =
        new ArrayList<>();

    public final ArrayList<ViridianBlocks.HeartBuild> hearts =
        new ArrayList<>();

    private final Map<ViridianBlocks.VesselBuild,
        ArrayList<ViridianBlocks.HeartBuild>> adjacentHearts =
        new HashMap<>();

    private float lastUpdateTime = Float.NaN;

    // ---------------------------------------------------------
    // REGISTER / UNREGISTER
    // ---------------------------------------------------------

    public static void register(ViridianBlocks.VesselBuild vessel) {
        if(vessel == null) return;

        if(!registered.contains(vessel)) {
            registered.add(vessel);
        }

        rebuild();
    }

    public static void unregister(ViridianBlocks.VesselBuild vessel) {
        if(vessel == null) return;

        registered.remove(vessel);
        rebuild();
    }

    // ---------------------------------------------------------
    // REBUILD NETWORK
    // ---------------------------------------------------------

    public static void rebuild() {

        if(Vars.world == null) {
            for(ViridianBlocks.VesselBuild vessel : registered) {
                if(vessel != null) vessel.bioGraph = null;
            }
            return;
        }

        // Remove invalid vessels and clear old graph references.
        for(int i = registered.size() - 1; i >= 0; i--) {
            ViridianBlocks.VesselBuild vessel = registered.get(i);

            if(vessel == null || !vessel.isAdded()) {
                registered.remove(i);
            } else {
                vessel.bioGraph = null;
            }
        }

        Set<ViridianBlocks.VesselBuild> known =
            new HashSet<>(registered);

        Map<ViridianBlocks.VesselBuild,
            ArrayList<ViridianBlocks.VesselBuild>> vesselLinks =
                new HashMap<>();

        Map<ViridianBlocks.VesselBuild,
            ArrayList<ViridianBlocks.HeartBuild>> vesselHearts =
                new HashMap<>();

        Map<ViridianBlocks.HeartBuild,
            ArrayList<ViridianBlocks.VesselBuild>> heartLinks =
                new HashMap<>();

        // Discover eight-direction connections.
        for(ViridianBlocks.VesselBuild vessel : registered) {

            ArrayList<ViridianBlocks.VesselBuild> neighbors =
                new ArrayList<>();

            ArrayList<ViridianBlocks.HeartBuild> nearbyHearts =
                new ArrayList<>();

            for(Point2 point : Geometry.d8) {

                Building other = Vars.world.build(
                    vessel.tileX() + point.x,
                    vessel.tileY() + point.y
                );

                if(other instanceof ViridianBlocks.VesselBuild) {

                    ViridianBlocks.VesselBuild next =
                        (ViridianBlocks.VesselBuild)other;

                    if(next != vessel
                        && known.contains(next)
                        && !neighbors.contains(next)) {
                        neighbors.add(next);
                    }

                } else if(other instanceof ViridianBlocks.HeartBuild) {

                    ViridianBlocks.HeartBuild heart =
                        (ViridianBlocks.HeartBuild)other;

                    if(heart.isAdded()
                        && !nearbyHearts.contains(heart)) {
                        nearbyHearts.add(heart);
                    }
                }
            }

            vesselLinks.put(vessel, neighbors);
            vesselHearts.put(vessel, nearbyHearts);

            for(ViridianBlocks.HeartBuild heart : nearbyHearts) {

                ArrayList<ViridianBlocks.VesselBuild> linked =
                    heartLinks.computeIfAbsent(
                        heart,
                        key -> new ArrayList<>()
                    );

                if(!linked.contains(vessel)) {
                    linked.add(vessel);
                }
            }
        }

        // Build connected components using BFS.
        Set<ViridianBlocks.VesselBuild> visited =
            new HashSet<>();

        for(ViridianBlocks.VesselBuild start : registered) {

            if(!visited.add(start)) continue;

            BioEnergyGraph graph = new BioEnergyGraph();

            ArrayDeque<ViridianBlocks.VesselBuild> queue =
                new ArrayDeque<>();

            queue.addLast(start);

            while(!queue.isEmpty()) {

                ViridianBlocks.VesselBuild current =
                    queue.removeFirst();

                graph.vessels.add(current);
                current.bioGraph = graph;

                ArrayList<ViridianBlocks.HeartBuild> currentHearts =
                    vesselHearts.get(current);

                if(currentHearts != null) {

                    graph.adjacentHearts.put(
                        current,
                        new ArrayList<>(currentHearts)
                    );

                    for(ViridianBlocks.HeartBuild heart : currentHearts) {

                        if(!graph.hearts.contains(heart)) {
                            graph.hearts.add(heart);
                        }

                        // Vessels touching the same Heart
                        // belong to the same connected network.
                        ArrayList<ViridianBlocks.VesselBuild> linked =
                            heartLinks.get(heart);

                        if(linked != null) {
                            for(ViridianBlocks.VesselBuild next : linked) {
                                if(visited.add(next)) {
                                    queue.addLast(next);
                                }
                            }
                        }
                    }
                }

                ArrayList<ViridianBlocks.VesselBuild> neighbors =
                    vesselLinks.get(current);

                if(neighbors != null) {
                    for(ViridianBlocks.VesselBuild next : neighbors) {
                        if(visited.add(next)) {
                            queue.addLast(next);
                        }
                    }
                }
            }
        }
    }

    // ---------------------------------------------------------
    // GRAPH LOOKUP
    // ---------------------------------------------------------

    public static BioEnergyGraph getGraph(Building building) {

        if(building instanceof ViridianBlocks.VesselBuild) {
            return ((ViridianBlocks.VesselBuild)building).bioGraph;
        }

        return null;
    }

    // ---------------------------------------------------------
    // ADD / REMOVE ENERGY
    // ---------------------------------------------------------

    public void addEnergy(float amount) {

        if(amount <= 0f || vessels.isEmpty()) return;

        float remaining = amount;

        for(ViridianBlocks.VesselBuild vessel : vessels) {

            float free = vessel.maxBioEnergy - vessel.bioEnergy;

            if(free <= 0f) continue;

            float accepted = Math.min(free, remaining);

            vessel.addBioEnergy(accepted);
            remaining -= accepted;

            if(remaining <= 0f) break;
        }
    }

    public void removeEnergy(float amount) {

        if(amount <= 0f || vessels.isEmpty()) return;

        float remaining = amount;

        for(ViridianBlocks.VesselBuild vessel : vessels) {

            if(vessel.bioEnergy <= 0f) continue;

            float removed = Math.min(vessel.bioEnergy, remaining);

            vessel.removeBioEnergy(removed);
            remaining -= removed;

            if(remaining <= 0f) break;
        }
    }

    // ---------------------------------------------------------
    // NETWORK UPDATE
    // ---------------------------------------------------------

    public void update() {

        // A graph must only update once per game frame.
        if(lastUpdateTime == Time.time) return;

        lastUpdateTime = Time.time;

        if(vessels.isEmpty()) return;

        float transferAmount =
            ViridianBlocks.VesselBuild.transferRatePerSecond
            * Time.delta / 60f;

        if(transferAmount <= 0f) return;

        // Hysteresis thresholds prevent Heart and Vessel
        // from continuously transferring energy back and forth.
        boolean returnedEnergy = false;

        float reserve = ViridianBlocks.VesselBuild.heartReturnReserve;

        Map<ViridianBlocks.VesselBuild, Float> returnedByVessel =
            new HashMap<>();

        // STEP 1: Return surplus energy to a low-energy Heart.
        // Every Vessel retains at least the configured reserve.
        for(ViridianBlocks.HeartBuild heart : hearts) {

            if(heart == null || !heart.isAdded()) continue;

            float returnThreshold = heart.maxBioEnergy * 0.25f;
            float targetEnergy = heart.maxBioEnergy * 0.75f;

            if(heart.bioEnergy >= returnThreshold) continue;

            float needed = targetEnergy - heart.bioEnergy;

            if(needed <= 0f) continue;

            for(ViridianBlocks.VesselBuild vessel : vessels) {

                if(needed <= 0f) break;

                float alreadyReturned =
                    returnedByVessel.getOrDefault(vessel, 0f);

                float available =
                    vessel.bioEnergy - reserve;

                float rateLeft =
                    transferAmount - alreadyReturned;

                if(available <= 0f || rateLeft <= 0f) continue;

                float amount = Math.min(
                    needed,
                    Math.min(available, rateLeft)
                );

                vessel.removeBioEnergy(amount);
                heart.addBioEnergy(amount);

                returnedByVessel.put(
                    vessel,
                    alreadyReturned + amount
                );

                needed -= amount;
                returnedEnergy = true;
            }
        }

        // Do not send energy back to the Vessel network
        // in the same frame in which energy returned to Heart.
        if(!returnedEnergy) {

            Map<ViridianBlocks.VesselBuild, Float> receivedByVessel =
                new HashMap<>();

            // STEP 2: Heart supplies directly adjacent Vessels
            // when it has sufficient energy.
            for(ViridianBlocks.VesselBuild vessel : vessels) {

                ArrayList<ViridianBlocks.HeartBuild> nearby =
                    adjacentHearts.get(vessel);

                if(nearby == null) continue;

                for(ViridianBlocks.HeartBuild heart : nearby) {

                    if(heart == null || !heart.isAdded()) continue;

                    float supplyThreshold =
                        heart.maxBioEnergy * 0.75f;

                    float available =
                        heart.bioEnergy - supplyThreshold;

                    if(available <= 0f) continue;

                    float alreadyReceived =
                        receivedByVessel.getOrDefault(vessel, 0f);

                    float rateLeft =
                        transferAmount - alreadyReceived;

                    float free =
                        vessel.maxBioEnergy - vessel.bioEnergy;

                    if(free <= 0f || rateLeft <= 0f) continue;

                    float amount = Math.min(
                        available,
                        Math.min(free, rateLeft)
                    );

                    heart.removeBioEnergy(amount);
                    vessel.addBioEnergy(amount);

                    receivedByVessel.put(
                        vessel,
                        alreadyReceived + amount
                    );
                }
            }
        }

        // STEP 3: Balance energy across the entire network.
        // Calculate all transfers first, then apply them together.
        // This avoids creating or losing energy during balancing.
        int count = vessels.size();

        if(count <= 1) return;

        float total = 0f;

        for(ViridianBlocks.VesselBuild vessel : vessels) {
            total += vessel.bioEnergy;
        }

        float average = total / count;

        float[] outgoing = new float[count];
        float[] incoming = new float[count];

        float outgoingTotal = 0f;
        float incomingTotal = 0f;

        for(int i = 0; i < count; i++) {

            ViridianBlocks.VesselBuild vessel = vessels.get(i);

            float energy = vessel.bioEnergy;

            if(energy > average) {

                outgoing[i] = Math.min(
                    energy - average,
                    transferAmount
                );

                outgoingTotal += outgoing[i];

            } else if(energy < average) {

                float free =
                    vessel.maxBioEnergy - energy;

                incoming[i] = Math.min(
                    average - energy,
                    Math.min(transferAmount, free)
                );

                incomingTotal += incoming[i];
            }
        }

        float transferable = Math.min(
            outgoingTotal,
            incomingTotal
        );

        if(transferable <= 0f) return;

        float outgoingScale =
            outgoingTotal > 0f
            ? transferable / outgoingTotal
            : 0f;

        float incomingScale =
            incomingTotal > 0f
            ? transferable / incomingTotal
            : 0f;

        for(int i = 0; i < count; i++) {

            ViridianBlocks.VesselBuild vessel = vessels.get(i);

            float change =
                incoming[i] * incomingScale
                - outgoing[i] * outgoingScale;

            if(change > 0f) {
                vessel.addBioEnergy(change);
            } else if(change < 0f) {
                vessel.removeBioEnergy(-change);
            }
        }
    }
}
n(
                need,
                Math.min(transferAmount, pool)
            );

            vessel.addBioEnergy(amount);
            pool -= amount;

            if(pool <= 0f) break;
        }

        // Phần pool còn lại nếu có không bị tạo hoặc xóa:
        // nó chỉ có thể tồn tại do giới hạn tốc độ phân phối.
        // Năng lượng đã được trừ khỏi nguồn ở trên.
        if(pool > 0f) {
            for(int i = 0; i < vessels.size() && pool > 0f; i++) {

                ViridianBlocks.VesselBuild vessel = vessels.get(i);

                float free = vessel.maxBioEnergy - vessel.bioEnergy;
                if(free <= 0f) continue;

                float amount = Math.min(free, pool);

                vessel.addBioEnergy(amount);
                pool -= amount;
            }
        }
    }
}
