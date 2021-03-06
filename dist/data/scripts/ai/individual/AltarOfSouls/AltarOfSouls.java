package ai.individual.AltarOfSouls;

import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.quest.Quest;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LasTravel
 */

public class AltarOfSouls extends Quest {
	private static final String qn = "AltarOfSouls";
	private static Map<Integer, Boolean> spawnInfo = new HashMap<Integer, Boolean>(3);
	private static final int[] raidIds = {25944, 25943, 25942};
	private static final int[] stoneIds = {38572, 38573, 38574};
	private static final int altarOfSoulsId = 33920;

	public AltarOfSouls(int questId, String name, String descr) {
		super(questId, name, descr);

		addFirstTalkId(altarOfSoulsId);
		addStartNpc(altarOfSoulsId);
		addTalkId(altarOfSoulsId);

		for (int i : raidIds) {
			addKillId(i);
			spawnInfo.put(i, false);
		}
	}

	@Override
	public String onFirstTalk(Npc npc, Player player) {
		return "AltarOfSouls.html";
	}

	@Override
	public String onAdvEvent(String event, Npc npc, Player player) {
		if (event.startsWith("trySpawnBoss")) {
			int bossId = Integer.valueOf(event.split(" ")[1]);
			int stoneId = 0;
			if (bossId == raidIds[0]) {
				stoneId = stoneIds[0];
			} else if (bossId == raidIds[1]) {
				stoneId = stoneIds[1];
			} else {
				stoneId = stoneIds[2];
			}

			if (stoneId == 0) //Cheating?
			{
				return null;
			}

			synchronized (spawnInfo) {
				if (!spawnInfo.get(bossId)) {
					if (!player.destroyItemByItemId(qn, stoneId, 1, player, true)) {
						return stoneId + "-no.html";
					}

					spawnInfo.put(bossId, true); //Boss is spawned

					Attackable boss = (Attackable) addSpawn(bossId, npc.getX(), npc.getY() + 200, npc.getZ(), 0, false, 0, true);
					boss.setIsRunning(true);
					boss.setTarget(player);
					boss.addDamageHate(player, 9999, 9999);
					boss.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
				}
			}
		}

		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onKill(Npc npc, Player player, boolean isPet) {
		synchronized (spawnInfo) {
			spawnInfo.put(npc.getNpcId(), false);
		}

		return super.onKill(npc, player, isPet);
	}

	public static void main(String[] args) {
		new AltarOfSouls(-1, qn, "ai/individual");
	}
}
