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

package l2server.gameserver.network.serverpackets;

import l2server.Config;
import l2server.gameserver.datatables.CharNameTable;
import l2server.gameserver.network.clientpackets.CharacterCreate;
import l2server.gameserver.util.Util;

/**
 * @author Pere
 */
public final class ExIsCharNameCreatable extends L2GameServerPacket {
	private int result;
	
	public ExIsCharNameCreatable(String name) {
		if (name.length() < 1 || name.length() > 16) {
			result = CharCreateFail.REASON_16_ENG_CHARS;
			return;
		}
		
		if (Config.FORBIDDEN_NAMES.length > 1) {
			for (String st : Config.FORBIDDEN_NAMES) {
				if (name.toLowerCase().contains(st.toLowerCase())) {
					result = CharCreateFail.REASON_INCORRECT_NAME;
					return;
				}
			}
		}
		
		// Last Verified: May 30, 2009 - Gracia Final
		if (!Util.isAlphaNumeric(name) || !CharacterCreate.isValidName(name)) {
			result = CharCreateFail.REASON_INCORRECT_NAME;
			return;
		}
		
		if (CharNameTable.getInstance().doesCharNameExist(name)) {
			result = CharCreateFail.REASON_NAME_ALREADY_EXISTS;
			return;
		}
		
		result = -1;
	}
	
	@Override
	protected final void writeImpl() {
		writeD(result);
	}
}
