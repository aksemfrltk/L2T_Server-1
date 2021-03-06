package instances.DimensionalDoor.BloodThirst;

import ai.group_template.L2AttackableAIScript;
import instances.DimensionalDoor.DimensionalDoor;
import l2server.Config;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.NpcBufferInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.entity.Instance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * @author LasTravel
 * <p>
 * Blood Thirst
 * <p>
 * Source:
 * - https://www.youtube.com/watch?v=t-rLTz-ACE
 * - https://www.youtube.com/watch?v=ElaX6oM5l1g
 */

public class BloodThirst extends L2AttackableAIScript {
	//Quest
	private static final boolean debug = false;
	private static final String qn = "BloodThirst";

	//Ids
	private static final int instanceTemplateId = 505;
	private static final int bloodThirstId = 27481;
	private static final int reuseMinutes = 1440;

	public BloodThirst(int questId, String name, String descr) {
		super(questId, name, descr);

		addTalkId(DimensionalDoor.getNpcManagerId());
		addStartNpc(DimensionalDoor.getNpcManagerId());

		addKillId(bloodThirstId);
	}

	private class BloodThirstWorld extends InstanceWorld {
		private Npc bloodThirst;

		private BloodThirstWorld() {
		}
	}

	@Override
	public final String onAdvEvent(String event, Npc npc, Player player) {
		if (debug) {
			log.warn(getName() + ": onAdvEvent: " + event);
		}

		InstanceWorld wrld = null;
		if (npc != null) {
			wrld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		} else if (player != null) {
			wrld = InstanceManager.getInstance().getPlayerWorld(player);
		} else {
			log.warn(getName() + ": onAdvEvent: Unable to get world.");
			return null;
		}

		if (wrld != null && wrld instanceof BloodThirstWorld) {
			BloodThirstWorld world = (BloodThirstWorld) wrld;
			if (event.equalsIgnoreCase("stage_1_start")) {
				InstanceManager.getInstance().stopWholeInstance(world.instanceId);
				InstanceManager.getInstance().showVidToInstance(109, world.instanceId);

				world.bloodThirst = addSpawn(bloodThirstId, 56167, -186938, -7944, 16383, false, 0, true, world.instanceId);

				for (L2Spawn iSpawn : SpawnTable.getInstance().getSpecificSpawns("blood_thirst")) {
					if (iSpawn == null) {
						continue;
					}

					Npc iNpc = addSpawn(iSpawn.getNpcId(),
							iSpawn.getX(),
							iSpawn.getY(),
							iSpawn.getZ(),
							iSpawn.getHeading(),
							false,
							0,
							true,
							world.instanceId);

					L2Spawn spawn = iNpc.getSpawn();
					spawn.setRespawnDelay(20);
					spawn.startRespawn();
				}

				final int instanceId = world.instanceId;
				ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() {
					@Override
					public void run() {
						InstanceManager.getInstance().startWholeInstance(instanceId);
					}
				}, 13000);
			}
		} else if (event.equalsIgnoreCase("enterToInstance")) {
			try {
				enterInstance(player);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public String onKill(Npc npc, Player player, boolean isPet) {
		if (debug) {
			log.warn(getName() + ": onKill: " + npc.getName());
		}

		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof BloodThirstWorld) {
			BloodThirstWorld world = (BloodThirstWorld) tmpworld;
			if (npc == world.bloodThirst) {
				player.addItem(qn, DimensionalDoor.getDimensionalDoorRewardId(), 10, player, true);

				InstanceManager.getInstance().setInstanceReuse(world.instanceId, instanceTemplateId, reuseMinutes);
				InstanceManager.getInstance().finishInstance(world.instanceId, true);
			}
		}
		return "";
	}

	@Override
	public final String onTalk(Npc npc, Player player) {
		if (debug) {
			log.warn(getName() + ": onTalk: " + player.getName());
		}

		if (npc.getNpcId() == DimensionalDoor.getNpcManagerId()) {
			return qn + ".html";
		}

		return super.onTalk(npc, player);
	}

	private final synchronized void enterInstance(Player player) {
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null) {
			if (!(world instanceof BloodThirstWorld)) {
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return;
			}

			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			if (inst != null) {
				if (inst.getInstanceEndTime() > 300600 && world.allowed.contains(player.getObjectId())) {
					player.setInstanceId(world.instanceId);
					player.teleToLocation(56164, -185809, -7944);
				}
			}

			return;
		} else {
			if (!debug && !InstanceManager.getInstance().checkInstanceConditions(player, instanceTemplateId, 1, 1, 99, Config.MAX_LEVEL)) {
				return;
			}

			final int instanceId = InstanceManager.getInstance().createDynamicInstance(qn + ".xml");
			world = new BloodThirstWorld();
			world.instanceId = instanceId;
			world.status = 0;

			InstanceManager.getInstance().addWorld(world);

			world.allowed.add(player.getObjectId());

			player.stopAllEffectsExceptThoseThatLastThroughDeath();
			player.setInstanceId(instanceId);
			player.teleToLocation(56164, -185809, -7944, true);

			NpcBufferInstance.giveBasicBuffs(player);

			startQuestTimer("stage_1_start", 20000, null, player);

			log.debug(getName() + ":  instance started: " + instanceId + " created by player: " + player.getName());

			return;
		}
	}

	public static void main(String[] args) {
		new BloodThirst(-1, qn, "instances/DimensionalDoor");
	}
}
