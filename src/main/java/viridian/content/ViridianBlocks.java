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

            public class HeartBuild extends Building {

                // =========================
                // BIO-ENERGY
                // =========================

                public float bioEnergy = 100f;
                public float maxBioEnergy = 100f;


                @Override
                public void updateTile() {
                    // Chưa tiêu thụ năng lượng.
                    // Tạm thời giữ nguyên 100 / 100.
                }
            }


            // =========================
            // BARS
            // =========================

            @Override
            public void setBars() {

                // Giữ thanh HP mặc định.
                super.setBars();

                // Thanh Bio-Energy.
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

Lần test này

Nếu Actions xanh → tải ".jar" và vào game đặt Heart.

Ta cần thấy:

Health — thanh đỏ mặc định
Bio-Energy — thanh vàng "100/100"

API của v160.5 cho phép "addBar()" nhận một "Building" rồi tạo "Bar" với tên, màu và giá trị 0–1, đúng với cách đoạn trên hoạt động.

Chưa làm 3 thanh còn lại. Khi thanh vàng chạy ổn, ta sẽ thêm tiếp 3 thanh vào cùng hệ thống mà không phải đụng lại phần Heart cơ bản. 😎
