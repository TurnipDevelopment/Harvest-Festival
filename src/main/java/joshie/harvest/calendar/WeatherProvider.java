package joshie.harvest.calendar;

import joshie.harvest.api.calendar.Season;
import joshie.harvest.api.calendar.Weather;
import joshie.harvest.api.core.ISeasonData;
import joshie.harvest.core.config.Calendar;
import joshie.harvest.core.handlers.HFTrackers;
import joshie.harvest.core.helpers.generic.MCClientHelper;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WeatherProvider extends WorldProviderSurface {
    @SideOnly(Side.CLIENT)
    private IRenderHandler WEATHER_RENDERER;

    @SideOnly(Side.CLIENT)
    @Override
    public IRenderHandler getWeatherRenderer() {
        if (!Calendar.ENABLE_FORECAST) return null;
        if (WEATHER_RENDERER != null) return WEATHER_RENDERER;
        else WEATHER_RENDERER = new WeatherRenderer();
        return WEATHER_RENDERER;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public float getStarBrightness(float f) {
        if (!Calendar.ENABLE_SEASONAL_SKY) return super.getStarBrightness(f);
        float brightness = super.getStarBrightness(f);
        return HFTrackers.getCalendar(MCClientHelper.getWorld()).getDate().getSeason() == Season.WINTER ? brightness * 1.25F : brightness;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public float getSunBrightness(float f) {
        if (!Calendar.ENABLE_SEASONAL_SKY) return super.getSunBrightness(f);
        float brightness = worldObj.getSunBrightnessBody(f);
        return HFTrackers.getCalendar(MCClientHelper.getWorld()).getDate().getSeason() == Season.SUMMER ? brightness * 1.25F : brightness;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Vec3d getSkyColor(Entity cameraEntity, float partialTicks) {
        if (!Calendar.ENABLE_SEASONAL_SKY) return super.getSkyColor(cameraEntity, partialTicks);
        float f1 = worldObj.getCelestialAngle(partialTicks);
        float f2 = MathHelper.cos(f1 * (float) Math.PI * 2.0F) * 2.0F + 0.5F;

        if (f2 < 0.0F) {
            f2 = 0.0F;
        }

        if (f2 > 1.0F) {
            f2 = 1.0F;
        }


        int l = HFTrackers.getCalendar(MCClientHelper.getWorld()).getSeasonData().getSkyColor();
        float f4 = (float) (l >> 16 & 255) / 255.0F;
        float f5 = (float) (l >> 8 & 255) / 255.0F;
        float f6 = (float) (l & 255) / 255.0F;
        f4 *= f2;
        f5 *= f2;
        f6 *= f2;
        float f7 = Math.min(1F, worldObj.getRainStrength(partialTicks));
        float f8;
        float f9;

        if (f7 > 0.0F) {
            f8 = (f4 * 0.3F + f5 * 0.59F + f6 * 0.11F) * 0.6F;
            f9 = 1.0F - f7 * 0.75F;
            f4 = f4 * f9 + f8 * (1.0F - f9);
            f5 = f5 * f9 + f8 * (1.0F - f9);
            f6 = f6 * f9 + f8 * (1.0F - f9);
        }

        f8 = worldObj.getThunderStrength(partialTicks);

        if (f8 > 0.0F) {
            f8 /= 10F;
            f9 = (f4 * 0.3F + f5 * 0.59F + f6 * 0.11F) * 0.2F;
            float f10 = 1.0F - f8 * 0.75F;
            f4 = f4 * f10 + f9 * (1.0F - f10);
            f5 = f5 * f10 + f9 * (1.0F - f10);
            f6 = f6 * f10 + f9 * (1.0F - f10);
        }

        if (worldObj.getLastLightningBolt() > 0) {
            f9 = (float) worldObj.getLastLightningBolt() - partialTicks;

            if (f9 > 1.0F) {
                f9 = 1.0F;
            }

            f9 *= 0.45F;
            f4 = f4 * (1.0F - f9) + 0.8F * f9;
            f5 = f5 * (1.0F - f9) + 0.8F * f9;
            f6 = f6 * (1.0F - f9) + 1.0F * f9;
        }

        return new Vec3d((double) f4, (double) f5, (double) f6);
    }

    public double clamp(double min, double max, double val) {
        return Math.max(min, Math.min(max, val));
    }

    /**
     * Cheers to chylex for a bunch of the maths on this one ;D
     **/
    @Override
    public float calculateCelestialAngle(long worldTime, float partialTicks) {
        if (!Calendar.ENABLE_DAY_LENGTH) return super.calculateCelestialAngle(worldTime, partialTicks);
        ISeasonData data = HFTrackers.getCalendar(worldObj).getDate().getSeasonData();
        if (data == null) return 1F;
        int time = (int) (worldTime % Calendar.TICKS_PER_DAY);
        double fac = data.getCelestialLengthFactor();
        float chylex = (float) (clamp(0, 1000D, time) + 11000D * (clamp(0, 11000D, time - 1000D) / 11000D) * fac + clamp(0, 1000D, time - 12000D) + 11000D * (clamp(0, 11000D, time - 12000D) / 11000D) * (2 - fac));
        float angle = (chylex / Calendar.TICKS_PER_DAY) - 0.25F;
        return angle + data.getCelestialAngleOffset();
    }

    @Override
    public boolean isBlockHighHumidity(BlockPos pos) {
        if (!Calendar.ENABLE_FORECAST) return super.isBlockHighHumidity(pos);
        return HFTrackers.getCalendar(worldObj).getDate().getSeason() != Season.SUMMER && super.isBlockHighHumidity(pos);
    }

    @Override
    public boolean canSnowAt(BlockPos pos, boolean checkLight) {
        if (!Calendar.ENABLE_FORECAST) return super.canSnowAt(pos, checkLight);
        Weather weather = HFTrackers.getCalendar(worldObj).getTodaysWeather();
        if (weather == Weather.SNOW || weather == Weather.BLIZZARD) {
            Biome biome = worldObj.getBiome(pos);
            float f = biome.getFloatTemperature(pos);

            if (f > 0.15F) {
                if (!checkLight) {
                    return true;
                } else {
                    if (biome.canRain()) {
                        if (pos.getY() >= 0 && pos.getY() < 256 && worldObj.getLightFor(EnumSkyBlock.BLOCK, pos) < 10) {
                            IBlockState state = worldObj.getBlockState(pos);
                            if (state.getMaterial() == Material.AIR && Blocks.SNOW_LAYER.canPlaceBlockAt(worldObj, pos)) {
                                return true;
                            }
                        }
                    }

                    return false;
                }
            } else return super.canSnowAt(pos, checkLight);
        } else return super.canSnowAt(pos, checkLight);
    }

    @Override
    public void resetRainAndThunder() {
        if (!Calendar.ENABLE_FORECAST) super.resetRainAndThunder();
    }

    @Override
    public void updateWeather() {
        if (!Calendar.ENABLE_FORECAST) super.updateWeather();
        if (!worldObj.isRemote) {
            joshie.harvest.calendar.Calendar calendar = HFTrackers.getCalendar(worldObj);
            float rainStrength = calendar.getTodaysRainStrength();
            float thunderStrength = calendar.getTodaysStormStrength();
            if (calendar.getTodaysWeather().isRain()) {
                HFTrackers.getCropTracker(worldObj).doRain();
            }

            if (worldObj.rainingStrength > rainStrength) {
                worldObj.rainingStrength -= 0.01F;
            } else if (worldObj.rainingStrength < rainStrength) {
                worldObj.rainingStrength += 0.01F;
            }

            if (worldObj.thunderingStrength > thunderStrength) {
                worldObj.thunderingStrength -= 0.1F;
            } else if (worldObj.thunderingStrength < thunderStrength) {
                worldObj.thunderingStrength += 0.01F;
            }
        }
    }
}