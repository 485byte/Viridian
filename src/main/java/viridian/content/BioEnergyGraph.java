
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

    private final Map<ViridianBlocks.VesselBuild,
        ArrayList<ViridianBlocks.VesselBuild>> adjacentVessels =
        new HashMap<>();

    private float lastUpdateTime = Float.NaN;

    // REGISTER / UNREGISTER

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

    // FIND HEARTS TOUCHING A VESSEL

    private static void addAdjacentHearts(
        ViridianBlocks.VesselBuild vessel,
        ArrayList<ViridianBlocks.HeartBuild> result
    ) {
        for(Building other : vessel.proximity) {
            if(other instanceof ViridianBlocks.HeartBuild) {
                ViridianBlocks.HeartBuild heart =
                    (ViridianBlocks.HeartBuild)other;

                if(heart.isAdded() && !result.contains(heart)) {
                    result.add(heart);
                }
            }
        }

        if(Vars.world == null) return;

        for(Point2 point : Geometry.d8) {
            Building other = Vars.world.build(
                vessel.tileX() + point.x,
                vessel.tileY() + point.y
            );

            if(other instanceof ViridianBlocks.HeartBuild) {
                ViridianBlocks.HeartBuild heart =
                    (ViridianBlocks.HeartBuild)other;

                if(heart.isAdded() && !result.contains(heart)) {
                    result.add(heart);
                }
            }
        }
    }

    // REBUILD CONNECTED NETWORKS

    public static void rebuild() {
        if(Vars.world == null) {
            for(ViridianBlocks.VesselBuild vessel : registered) {
                if(vessel != null) vessel.bioGraph = null;
            }
            return;
        }

        for(int i = registered.size() - 1; i >= 0; i--) {
            ViridianBlocks.VesselBuild vessel = registered.get(i);

            if(vessel == null || !vessel.isAdded()) {
                registered.remove(i);
            } else {
                vessel.bioGraph = null;
            }
        }

        Map<ViridianBlocks.VesselBuild,
            ArrayList<ViridianBlocks.VesselBuild>> vesselLinks =
                new HashMap<>();

        Map<ViridianBlocks.VesselBuild,
            ArrayList<ViridianBlocks.HeartBuild>> vesselHearts =
                new HashMap<>();

        Map<ViridianBlocks.HeartBuild,
            ArrayList<ViridianBlocks.VesselBuild>> heartLinks =
                new HashMap<>();

        Set<ViridianBlocks.VesselBuild> known =
            new HashSet<>(registered);

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
                }
            }

            addAdjacentHearts(vessel, nearbyHearts);

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
                    graph.adjacentVessels.put(
                        current,
                        new ArrayList<>(neighbors)
                    );

                    for(ViridianBlocks.VesselBuild next : neighbors) {
                        if(visited.add(next)) {
                            queue.addLast(next);
                        }
                    }
                }
            }
        }
    }

    // ADD ENERGY TO NETWORK

    public void addEnergy(float amount) {
        if(amount <= 0f || vessels.isEmpty()) return;

        float remaining = amount;

        for(ViridianBlocks.VesselBuild vessel : vessels) {
            if(vessel == null || !vessel.isAdded()) continue;

            float free = vessel.maxBioEnergy - vessel.bioEnergy;
            if(free <= 0f) continue;

            float accepted = Math.min(free, remaining);

            vessel.addBioEnergy(accepted);
            remaining -= accepted;

            if(remaining <= 0f) break;
        }
    }

    // REMOVE ENERGY FROM NETWORK

    public void removeEnergy(float amount) {
        if(amount <= 0f || vessels.isEmpty()) return;

        float remaining = amount;

        for(ViridianBlocks.VesselBuild vessel : vessels) {
            if(vessel == null || !vessel.isAdded()) continue;
            if(vessel.bioEnergy <= 0f) continue;

            float removed = Math.min(vessel.bioEnergy, remaining);

            vessel.removeBioEnergy(removed);
            remaining -= removed;

            if(remaining <= 0f) break;
        }
    }

    // UPDATE NETWORK

    public void update() {
        // Update each network once per game frame.
        if(lastUpdateTime == Time.time) return;
        lastUpdateTime = Time.time;

        if(vessels.isEmpty()) return;

        float transferAmount =
            ViridianBlocks.VesselBuild.transferRatePerSecond
            * Time.delta / 60f;

        if(transferAmount <= 0f) return;

        // Minimum energy preserved when returning energy to Heart.
        float reserve = 2f;

        Map<ViridianBlocks.VesselBuild, Float> returnedByVessel =
            new HashMap<>();

        boolean returnedEnergy = false;

        // STEP 1: Return energy to Hearts below 25%.
        // Preserve 2 energy in every Vessel.

        for(ViridianBlocks.HeartBuild heart : hearts) {
            if(heart == null || !heart.isAdded()) continue;
            if(heart.maxBioEnergy <= 0f) continue;

            float threshold = heart.maxBioEnergy * 0.25f;
            float target = heart.maxBioEnergy * 0.75f;

            if(heart.bioEnergy >= threshold) continue;

            float needed = target - heart.bioEnergy;

            for(ViridianBlocks.VesselBuild vessel : vessels) {
                if(needed <= 0f) break;
                if(vessel == null || !vessel.isAdded()) continue;

                float alreadyReturned =
                    returnedByVessel.getOrDefault(vessel, 0f);

                float available = vessel.bioEnergy - reserve;
                float rateLeft = transferAmount - alreadyReturned;

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

        // STEP 2: Heart continuously supplies directly connected
        // Vessels while Heart energy is above 75%.
        // Vessels below 2 energy receive priority.

        if(!returnedEnergy) {
            Map<ViridianBlocks.VesselBuild, Float> receivedFromHeart =
                new HashMap<>();

            // Pass 0: Vessels below 2 energy.
            // Pass 1: Other directly connected Vessels.

            for(int priorityPass = 0; priorityPass < 2; priorityPass++) {
                for(ViridianBlocks.VesselBuild vessel : vessels) {
                    if(vessel == null || !vessel.isAdded()) continue;

                    if(priorityPass == 0 && vessel.bioEnergy >= 2f) {
                        continue;
                    }

                    if(priorityPass == 1 && vessel.bioEnergy < 2f) {
                        continue;
                    }

                    ArrayList<ViridianBlocks.HeartBuild> nearby =
                        adjacentHearts.get(vessel);

                    if(nearby == null) continue;

                    for(ViridianBlocks.HeartBuild heart : nearby) {
                        if(heart == null || !heart.isAdded()) continue;
                        if(heart.maxBioEnergy <= 0f) continue;

                        float threshold = heart.maxBioEnergy * 0.75f;
                        float available = heart.bioEnergy - threshold;

                        if(available <= 0f) continue;

                        float alreadyReceived =
                            receivedFromHeart.getOrDefault(vessel, 0f);

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

                        receivedFromHeart.put(
                            vessel,
                            alreadyReceived + amount
                        );
                    }
                }
            }
        }

        // STEP 3: Multi-layer Vessel-to-Vessel transfer.
        //
        // First, prioritize Vessels below 2 energy.
        // Then allow energy to pass through Vessels at 2 energy
        // so it can reach further layers of the network.
        //
        // A sender must retain at least 2 energy.
        // Repeat across the network to propagate energy through
        // multiple layers in the same frame.

        int count = vessels.size();
        if(count <= 1) return;

        Map<ViridianBlocks.VesselBuild, Float> sentThisFrame =
            new HashMap<>();

        Map<ViridianBlocks.VesselBuild, Float> receivedThisFrame =
            new HashMap<>();

        for(int round = 0; round < count; round++) {

            boolean transferredInRound = false;

            // First pass: fill Vessels below 2.
            // Second pass: fill Vessels from 2 toward capacity 3.

            for(int priorityPass = 0; priorityPass < 2; priorityPass++) {

                for(ViridianBlocks.VesselBuild sender : vessels) {
                    if(sender == null || !sender.isAdded()) continue;

                    float sentAlready =
                        sentThisFrame.getOrDefault(sender, 0f);

                    float rateLeft = transferAmount - sentAlready;

                    float available = sender.bioEnergy - reserve;

                    if(rateLeft <= 0f || available <= 0f) continue;

                    ArrayList<ViridianBlocks.VesselBuild> neighbors =
                        adjacentVessels.get(sender);

                    if(neighbors == null) continue;

                    for(ViridianBlocks.VesselBuild receiver : neighbors) {
                        if(receiver == null || !receiver.isAdded()) continue;
                        if(receiver == sender) continue;

                        if(priorityPass == 0
                            && receiver.bioEnergy >= 2f) {
                            continue;
                        }

                        if(priorityPass == 1
                            && (receiver.bioEnergy < 2f
                                || receiver.bioEnergy
                                    >= receiver.maxBioEnergy)) {
                            continue;
                        }

                        sentAlready =
                            sentThisFrame.getOrDefault(sender, 0f);

                        rateLeft = transferAmount - sentAlready;
                        available = sender.bioEnergy - reserve;

                        float receivedAlready =
                            receivedThisFrame.getOrDefault(receiver, 0f);

                        float receiveRateLeft =
                            transferAmount - receivedAlready;

                        float free =
                            receiver.maxBioEnergy - receiver.bioEnergy;

                        if(rateLeft <= 0f
                            || available <= 0f
                            || receiveRateLeft <= 0f
                            || free <= 0f) {
                            continue;
                        }

                        float amount = Math.min(
                            available,
                            Math.min(
                                free,
                                Math.min(rateLeft, receiveRateLeft)
                            )
                        );

                        if(amount <= 0f) continue;

                        sender.removeBioEnergy(amount);
                        receiver.addBioEnergy(amount);

                        sentThisFrame.put(
                            sender,
                            sentAlready + amount
                        );

                        receivedThisFrame.put(
                            receiver,
                            receivedAlready + amount
                        );

                        transferredInRound = true;
                    }
                }
            }

            if(!transferredInRound) break;
        }
    }
}
            }
        }

        // STEP 2: Hearts supply directly connected Vessels
        // when the Heart has more than 75% energy.

        if(!returnedEnergy) {
            Map<ViridianBlocks.VesselBuild, Float> receivedByVessel =
                new HashMap<>();

            for(ViridianBlocks.VesselBuild vessel : vessels) {
                ArrayList<ViridianBlocks.HeartBuild> nearby =
                    adjacentHearts.get(vessel);

                if(nearby == null) continue;

                for(ViridianBlocks.HeartBuild heart : nearby) {
                    if(heart == null || !heart.isAdded()) continue;
                    if(heart.maxBioEnergy <= 0f) continue;

                    float threshold = heart.maxBioEnergy * 0.75f;
                    float available = heart.bioEnergy - threshold;

                    if(available <= 0f) continue;

                    float alreadyReceived =
                        receivedByVessel.getOrDefault(vessel, 0f);

                    float rateLeft = transferAmount - alreadyReceived;
                    float free = vessel.maxBioEnergy - vessel.bioEnergy;

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

        // STEP 3: Prioritize Vessels below 2 energy.
        // Vessels above 2 can share their excess energy.
        // Calculate changes first to avoid order-dependent transfers.

        int count = vessels.size();
        if(count <= 1) return;

        float priorityLevel = 2f;

        float[] energy = new float[count];
        float[] outgoing = new float[count];
        float[] incoming = new float[count];

        float outgoingTotal = 0f;
        float incomingTotal = 0f;

        for(int i = 0; i < count; i++) {
            ViridianBlocks.VesselBuild vessel = vessels.get(i);

            if(vessel == null || !vessel.isAdded()) continue;

            energy[i] = vessel.bioEnergy;

            // Only energy above the priority reserve can be shared.
            if(energy[i] > priorityLevel) {
                outgoing[i] = Math.min(
                    energy[i] - priorityLevel,
                    transferAmount
                );
                outgoingTotal += outgoing[i];
            }

            // Vessels below the priority level receive energy first.
            if(energy[i] < priorityLevel) {
                float need = priorityLevel - energy[i];
                float free = Math.max(
                    0f,
                    vessel.maxBioEnergy - energy[i]
                );

                incoming[i] = Math.min(
                    need,
                    Math.min(transferAmount, free)
                );
                incomingTotal += incoming[i];
            }
        }

        float transferable = Math.min(outgoingTotal, incomingTotal);
        if(transferable <= 0f) return;

        float outgoingScale = outgoingTotal > 0f
            ? transferable / outgoingTotal
            : 0f;

        float incomingScale = incomingTotal > 0f
            ? transferable / incomingTotal
            : 0f;

        for(int i = 0; i < count; i++) {
            ViridianBlocks.VesselBuild vessel = vessels.get(i);

            if(vessel == null || !vessel.isAdded()) continue;

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
