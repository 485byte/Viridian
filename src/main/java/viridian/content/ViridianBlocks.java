package viridian.content;

import arc.graphics.Color;
import arc.util.Log;
import mindustry.content.Items;
import mindustry.gen.Building;
import mindustry.type.Category;
import mindustry.ui.Bar;
import mindustry.world.Block;

import static mindustry.type.ItemStack.with;

public class ViridianBlocks {

    public static Block heart;

    public static void load() {

        Log.info("=== VIRIDIAN: BLOCK LOAD START ===");

        heart = new Block("viridian-heart") {

            // =========================
            // HEART BUILD
            // =========================

            public class HeartBuild extends Building {

                // -------------------------
                // BIO-ENERGY
                // -------------------------

                /** Năng lượng hiện tại */
                public float bioEnergy = 100f;

                /** Năng lượng tối đa */
                public float maxBioEnergy = 100f;


                // -------------------------
                // FUTURE SYSTEMS
                // -------------------------

                // Chưa sử dụng ở v0.3.
                // Để dành cho các phiên bản sau.

                public float bioReserve = 0f;
                public float maxBioReserve = 100f;

                public float bioRecovery = 0f;
                public float maxBioRecovery = 100f;

                public float bioNetwork = 100f;
                public float maxBioNetwork = 100f;


                @Override
                public void updateTile() {

                    // Chưa có logic tiêu thụ / sản xuất năng lượng.
                    // Heart hiện tại giữ nguyên 100/100.
                }
            }


            // =========================
            // UI BARS
            // =========================

            @Override
            public void setBars() {

                // Giữ thanh máu mặc định của Mindustry.
                super.setBars();

                // ---------------------------------
                // BIO-ENERGY
                // ---------------------------------

                addBar(
                    "bio-energy",
                    (HeartBuild entity) -> new Bar(
                        () -> "Bio-Energy",
                        () -> Color.yellow,
                        () -> entity.maxBioEnergy <= 0f
                            ? 0f
                            : entity.bioEnergy / entity.maxBioEnergy
                    )
                );


                // ---------------------------------
                // FUTURE BAR 1
                // ---------------------------------
                //
                // Bio-Reserve
                //
                // Chưa bật ở v0.3.
                //
                // Sau này chỉ cần bỏ comment:
                //
                /*
                addBar(
                    "bio-reserve",
                    (HeartBuild entity) -> new Bar(
                        () -> "Bio-Reserve",
                        () -> Color.purple,
                        () -> entity.maxBioReserve <= 0f
                            ? 0f
                            : entity.bioReserve / entity.maxBioReserve
                    )
                );
                */


                // ---------------------------------
                // FUTURE BAR 2
                // ---------------------------------
                //
                // Bio-Recovery
                //
                /*
                addBar(
                    "bio-recovery",
                    (HeartBuild entity) -> new Bar(
                        () -> "Bio-Recovery",
                        () -> Color.cyan,
                        () -> entity.maxBioRecovery <= 0f
                            ? 0f
                            : entity.bioRecovery / entity.maxBioRecovery
                    )
                );
                */


                // ---------------------------------
                // FUTURE BAR 3
                // ---------------------------------
                //
                // Bio-Network
                //
                /*
                addBar(
                    "bio-network",
                    (HeartBuild entity) -> new Bar(
                        () -> "Bio-Network",
                        () -> Color.lime,
                        () -> entity.bioNetwork / entity.maxBioNetwork
                    )
                );
                */
            }
        };


        // =========================
        // BLOCK SETTINGS
        // =========================

        heart.size = 2;
        heart.health = 1000;
        heart.destructible = true;

        heart.requirements(
            Category.effect,
            with(Items.copper, 10)
        );


        // =========================
        // LOG
        // =========================

        Log.info(
            "=== VIRIDIAN: HEART CREATED: "
            + heart.name
            + " ==="
        );

        Log.info(
            "=== VIRIDIAN: BLOCK SETUP COMPLETE ==="
        );
    }
}

Kết quả bản này

Khi đặt Heart và chọn nó, phía trên sẽ có kiểu:

Health
🟥 "████████████████ 100%"

Bio-Energy
🟨 "████████████████ 100%"

Và quan trọng là Bio-Energy thực sự lấy từ "bioEnergy / maxBioEnergy", chứ không phải một thanh trang trí. "addBar()" của Mindustry được thiết kế chính xác cho việc tạo các thanh kiểu này.

Sau này khi làm hệ thống thật, ta chỉ cần cho:

bioEnergy -= 8f * delta();

hoặc nạp năng lượng:

bioEnergy += amount;

thì thanh sẽ tự cập nhật theo giá trị đó.

Mình cố tình chưa bật 3 thanh còn lại để Heart v0.3 không hiển thị những thông số chưa có ý nghĩa. Khi tới phần Reserve/Recovery/Vessel Network, chỉ cần bỏ comment là có ngay 4 thanh. 😎
