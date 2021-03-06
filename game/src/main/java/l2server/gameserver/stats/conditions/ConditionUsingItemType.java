/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver.stats.conditions;

import l2server.gameserver.model.Item;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.itemcontainer.Inventory;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.item.ArmorType;
import l2server.gameserver.templates.item.ItemTemplate;

/**
 * The Class ConditionUsingItemType.
 *
 * @author mkizub
 */
public final class ConditionUsingItemType extends Condition {
	private final boolean armor;
	private final int mask;

	/**
	 * Instantiates a new condition using item type.
	 *
	 * @param mask the mask
	 */
	public ConditionUsingItemType(int mask) {
		this.mask = mask;
		armor = (mask & (ArmorType.MAGIC.mask() | ArmorType.LIGHT.mask() | ArmorType.HEAVY.mask())) != 0;
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.stats.conditions.Condition#testImpl(l2server.gameserver.stats.Env)
	 */
	@Override
	public boolean testImpl(Env env) {
		if (!(env.player instanceof Player)) {
			return false;
		}
		Inventory inv = ((Player) env.player).getInventory();

		//If ConditionUsingItemType is one between Light, Heavy or Magic
		if (armor) {
			//Get the itemMask of the weared chest (if exists)
			Item chest = inv.getPaperdollItem(Inventory.PAPERDOLL_CHEST);
			if (chest == null) {
				return false;
			}
			int chestMask = chest.getItem().getItemMask();

			//If chest armor is different from the condition one return false
			if ((mask & chestMask) == 0) {
				return false;
			}

			//So from here, chest armor matches conditions

			int chestBodyPart = chest.getItem().getBodyPart();
			//return True if chest armor is a Full Armor
			if (chestBodyPart == ItemTemplate.SLOT_FULL_ARMOR) {
				return true;
			} else { //check legs armor
				Item legs = inv.getPaperdollItem(Inventory.PAPERDOLL_LEGS);
				if (legs == null) {
					return false;
				}
				int legMask = legs.getItem().getItemMask();
				//return true if legs armor matches too
				return (mask & legMask) != 0;
			}
		}
		return (mask & inv.getWearedMask()) != 0;
	}
}
