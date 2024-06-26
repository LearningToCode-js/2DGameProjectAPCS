public class OverworldTerrainGenerator extends TerrainGenerator {
    private final Noise mainNoise;
    private final Noise caveNoise;
    private final Noise oreNoise;
    private final double yCoefficient = 1;
    private final double caveWidth = 2;
    private final double caveDepthStart = 10;
    private final double caveDepthEnd = 30;

    private final double oreDepthStart = 10;
    private final double oreDepthEnd = 50;
    private double caveMultiplier = 1;
    public final int seed;

    public OverworldTerrainGenerator(int seed){
        this.seed = seed;

        mainNoise = new CompositeNoise(
                1, 1,
                new ValueNoise(0.1, 5, StaticRandom.getRandom(seed, 0)),
                new ValueNoise(0.005, 15, StaticRandom.getRandom(seed, 1)),
                new ValueNoise(0.01, 30, StaticRandom.getRandom(seed, 2))
        );

        caveNoise = new ValueNoise(0.1,10, StaticRandom.getRandom(seed, 3));
        oreNoise = new ValueNoise(0.1,10, StaticRandom.getRandom(seed, 15));
    }

    @Override
    public void setTile(Chunk chunk, int localX, int localY) {
        TileType main = null;
        BackgroundTileType bg = null;

        int x = chunk.toGlobalPosX(localX);
        int y = chunk.toGlobalPosY(localY);

        double geologicalDepth = (y*yCoefficient) + mainNoise.sample(x, y);

        if(geologicalDepth > 0) {
            main = Tiles.REDDIRT;
            bg = Tiles.REDDIRT.backgroundType;
        }
        if(geologicalDepth > 5) {
            main = Tiles.STONE;
            bg = Tiles.STONE.backgroundType;
        }
        if (geologicalDepth > 200) {
            main = Tiles.BASALT;
            bg = Tiles.BASALT.backgroundType;
        }

        if (geologicalDepth > 200) {
            caveMultiplier = 2.5;
        } else {
            caveMultiplier = 1;
        }
        double caveValue = caveNoise.sample(x, y);
        double caveThreshold = caveMultiplier * Util.invLerp(caveDepthStart, caveDepthEnd, geologicalDepth);

        if(caveValue > -caveThreshold && caveValue < caveThreshold){
            main = null;
        }

        double oreValue = oreNoise.sample(x, y);
        double oreThreshold = 0.5 * Util.invLerp(oreDepthStart, oreDepthEnd, geologicalDepth);
        if(oreValue > -oreThreshold && oreValue < oreThreshold){
            if(main != null && geologicalDepth > 50){
                main = Tiles.IRON;
            }
        }
        if(Math.random() < 0.0005 && main != null && geologicalDepth > 10) {
            main = Tiles.TREASURE;
        }
        chunk.mainTilemap.setTile(localX, localY, main);
        chunk.backgroundTilemap.setTile(localX, localY, bg);
    }
}
