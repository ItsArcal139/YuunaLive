package com.kakaouo.mods.yuunalive.entities;

import com.kakaouo.mods.yuunalive.annotations.PlayerName;
import com.kakaouo.mods.yuunalive.annotations.PlayerNickname;
import com.kakaouo.mods.yuunalive.annotations.PlayerSkin;
import com.kakaouo.mods.yuunalive.annotations.SpawnEggColor;
import com.kakaouo.mods.yuunalive.entities.ai.goal.YuunaLivePlayerTravelGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.StructureFeature;

import java.util.ArrayList;
import java.util.List;

@PlayerSkin(value = "textures/entities/yuuna/4.png", slim = true)
@PlayerName("SachiYuuna")
@PlayerNickname("優奈")
@SpawnEggColor(primary = 0xffffff, secondary = 0xff76a8)
public class YuunaEntity extends YuunaLivePlayerEntity implements Travellable {
    private boolean wantsToAdventure = false;
    private BlockPos travelTarget = BlockPos.ORIGIN;

    protected YuunaEntity(EntityType<YuunaEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initCustomGoals() {
        super.initCustomGoals();
        this.goalSelector.add(14, new YuunaLivePlayerTravelGoal<>(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, HostileEntity.class, 0,
                false, false, this::canAttack
        ));
    }

    @Override
    public TextColor getNickNameColor() {
        return TextColor.fromFormatting(Formatting.LIGHT_PURPLE);
    }

    @Override
    public boolean doesAttackYuuna() {
        return true;
    }

    @Override
    public boolean canUseCriticalHit() {
        return true;
    }

    @Override
    public boolean isAttractedByYuuna() {
        return false;
    }

    @Override
    public void onKilledOther(ServerWorld world, LivingEntity other) {
        super.onKilledOther(world, other);
        var effect = new StatusEffectInstance(StatusEffects.REGENERATION, 200, 1, false, true);
        addStatusEffect(effect);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("WantsToAdventure", wantsToAdventure);

        BlockPos target = travelTarget;
        if(target != null) {
            nbt.put("TravelTarget", NbtHelper.fromBlockPos(target));
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if(nbt.contains("WantsToAdventure", NbtElement.BYTE_TYPE)) {
            setWantsToAdventure(nbt.getBoolean("WantsToAdventure"));
        }

        if(nbt.contains("TravelTarget")) {
            setTravelTarget(NbtHelper.toBlockPos(nbt.getCompound("TravelTarget")));
        }
    }

    @Override
    public boolean doesWantToAdventure() {
        return wantsToAdventure;
    }

    @Override
    public void setWantsToAdventure(boolean flag) {
        wantsToAdventure = flag;
    }

    @Override
    public BlockPos getTravelTarget() {
        return travelTarget;
    }

    public void setTravelTarget(BlockPos pos) {
        travelTarget = pos;
    }

    public List<StructureFeature<?>> getAvailableStructureTypes() {
        var result = new ArrayList<StructureFeature<?>>();
        result.add(StructureFeature.VILLAGE);
        result.add(StructureFeature.MANSION);
        result.add(StructureFeature.SWAMP_HUT);
        result.add(StructureFeature.PILLAGER_OUTPOST);
        result.add(StructureFeature.RUINED_PORTAL);
        return result;
    }

    public StructureFeature<?> getRandomStructureType() {
        var list = getAvailableStructureTypes();
        return list.get(getRandom().nextInt(list.size()));
    }

    @Override
    public void tick() {
        super.tick();
        if(!(world instanceof ServerWorld sw)) return;

        if(isAlive()) {
            LivingEntity target = getTarget();
            if(target != null && target.isDead()) {
                setTarget(null);
                var effect = new StatusEffectInstance(StatusEffects.REGENERATION, 200, 1, false, true);
                addStatusEffect(effect);
            }

            if(!wantsToAdventure) {
                if (getRandom().nextInt(100) == 0) {
                    BlockPos found = sw.locateStructure(getRandomStructureType(), getBlockPos(), 12, false);
                    if(travelTarget != null && !travelTarget.equals(found)) {
                        setTravelTarget(found);
                        wantsToAdventure = true;
                    }
                }
            }
        }
    }
}
