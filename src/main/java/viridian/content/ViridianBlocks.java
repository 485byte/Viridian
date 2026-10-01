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

                public float bioEnergy = 100f;
                public float maxBioEnergy = 100f;

                @Override
                public void updateTile() {
                    // Chưa có logic năng lượng.
                }
            }

            @Override
            public void setBars() {
                super.setBars();

                addBar(
                    "bio-energy",
                    new Bar(
                        "Bio-Energy",
                        Color.yellow,
                        () -> {
                            if (HeartBuild.this == null) return 0f;
                            return bioEnergy / maxBioEnergy;
                        }
                    )
                );
            }
        };

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

Khoan, có một lỗi trong chính đoạn trên: "HeartBuild.this" không thể dùng theo cách đó trong "Block" anonymous class. 😅 Vì vậy mình không muốn bạn copy một bản chưa kiểm chứng tiếp.

Cách an toàn hơn là đọc đúng lỗi compiler của run mới nhất rồi sửa đúng dòng đó. Hiện GitHub web của mình chưa tải được log run mới ("Sorry, something went wrong"), nên mình không thể trung thực nói chính xác “dòng X lỗi Y” được.

👉 Bạn mở run đỏ mới nhất → kéo xuống phần "buildJar" → bấm lỗi màu đỏ → gửi mình đúng 5–10 dòng quanh dòng "error:". Không cần chụp cả màn hình.

Mình sẽ sửa đúng lỗi đó, thay vì tiếp tục thử mò API. 😄
