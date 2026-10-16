
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

    private static final ArrayList<BioEnergyGraph> graphs =
        new ArrayList<>();

    public final ArrayList<ViridianBlocks.VesselBuild> vessels =
        new ArrayList<>();

    public final ArrayList<ViridianBlocks.HeartBuild> hearts =
        new ArrayList<>();

    private final Map<ViridianBlocks.VesselBuild,
        ArrayList<ViridianBlocks.HeartBuild>> adjacentHearts =
        new HashMap<>();

    private float lastUpdateTime = Float.NaN;

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

    public static void rebuild() {

        graphs.clear();

        // Loại bỏ Building không còn tồn tại.
        for(int i = registered.size() - 1; i >= 0; i--) {
            ViridianBlocks.VesselBuild vessel = registered.get(i);

            if(vessel == null || !vessel.isAdded()) {
                registered.remove(i);
            } else {
                vessel.bioGraph = null;
            }
        }

        if(Vars.world == null) return;

        // Danh sách Vessel đang tồn tại.
        Set<ViridianBlocks.VesselBuild> known =
            new HashSet<>(registered);

        // Vessel nào nằm cạnh Vessel nào.
        Map<ViridianBlocks.VesselBuild,
            ArrayList<ViridianBlocks.VesselBuild>> vesselLinks =
                new HashMap<>();

        // Vessel nào tiếp xúc trực tiếp với Heart nào.
        Map<ViridianBlocks.VesselBuild,
            ArrayList<ViridianBlocks.HeartBuild>> vesselHearts =
                new HashMap<>();

        // Các Vessel tiếp xúc với cùng một Heart.
        Map<ViridianBlocks.HeartBuild,
            ArrayList<ViridianBlocks.VesselBuild>> heartLinks =
                new HashMap<>();

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

                    if(next != vessel && known.contains(next)
                        && !neighbors.contains(next)) {
                        neighbors.add(next);
                    }

                } else if(other instanceof ViridianBlocks.HeartBuild) {

                    ViridianBlocks.HeartBuild heart =
                        (ViridianBlocks.HeartBuild)other;

                    if(!nearbyHearts.contains(heart)) {
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

        // Tìm từng thành phần liên thông bằng BFS.
        Set<ViridianBlocks.VesselBuild> visited =
            new HashSet<>();

        for(ViridianBlocks.VesselBuild start : registered) {

            if(visited.contains(start)) continue;

            BioEnergyGraph graph = new BioEnergyGraph();

            ArrayDeque<ViridianBlocks.VesselBuild> queue =
                new ArrayDeque<>();

            queue.add(start);
            visited.add(start);

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

                        // Heart là một nút kết nối chung.
                        // Các Vessel chạm cùng Heart thuộc cùng mạng.
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

            graphs.add(graph);
        }
    }

    public static BioEnergyGraph getGraph(Building building) {

        if(building instanceof ViridianBlocks.VesselBuild) {
            return ((ViridianBlocks.VesselBuild)building).bioGraph;
        }

        // Heart không nhất thiết phải thuộc một graph.
        // Source/Drain đặt cạnh Heart có thể tác động trực tiếp.
        return null;
    }

    public void addEnergy(float amount) {

        if(amount <= 0f || vessels.isEmpty()) return;

        float remaining = amount;

        // Phân phối vào các Vessel còn chỗ chứa.
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

            float removed = Math.min(
                vessel.bioEnergy,
                remaining
            );

            vessel.removeBioEnergy(removed);
            remaining -= removed;

            if(remaining <= 0f) break;
        }
    }

    public void update() {

        // Một graph chỉ cập nhật một lần trong cùng frame.
        if(lastUpdateTime == Time.time) return;

        lastUpdateTime = Time.time;

        if(vessels.isEmpty()) return;

        float transferAmount =
            ViridianBlocks.VesselBuild.transferRatePerSecond
            * Time.delta / 60f;

        // BƯỚC 1: Heart chỉ cấp năng lượng cho Vessel
        // mà nó tiếp xúc trực tiếp.
        for(ViridianBlocks.VesselBuild vessel : vessels) {

            ArrayList<ViridianBlocks.HeartBuild> nearby =
                adjacentHearts.get(vessel);

            if(nearby == null) continue;

            for(ViridianBlocks.HeartBuild heart : nearby) {

                if(!heart.isAdded()) continue;

                float free =
                    vessel.maxBioEnergy - vessel.bioEnergy;

                if(free <= 0f || heart.bioEnergy <= 0f) continue;

                float amount = Math.min(
                    transferAmount,
                    Math.min(free, heart.bioEnergy)
                );

                heart.removeBioEnergy(amount);
                vessel.addBioEnergy(amount);
            }
        }

        // BƯỚC 2: Cân bằng năng lượng trong mạng.
        // Mỗi Vessel có thể gửi/nhận tối đa transferAmount
        // trong bước cân bằng của frame này.
        float total = 0f;

        for(ViridianBlocks.VesselBuild vessel : vessels) {
            total += vessel.bioEnergy;
        }

        float average = total / vessels.size();

        float[] removed = new float[vessels.size()];
        float pool = 0f;

        for(int i = 0; i < vessels.size(); i++) {

            ViridianBlocks.VesselBuild vessel = vessels.get(i);

            float excess = vessel.bioEnergy - average;

            if(excess <= 0f) continue;

            float amount = Math.min(excess, transferAmount);

            vessel.removeBioEnergy(amount);

            removed[i] = amount;
            pool += amount;
        }

        for(ViridianBlocks.VesselBuild vessel : vessels) {

            float need = average - vessel.bioEnergy;

            if(need <= 0f || pool <= 0f) continue;

            float amount = Math.min(
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
