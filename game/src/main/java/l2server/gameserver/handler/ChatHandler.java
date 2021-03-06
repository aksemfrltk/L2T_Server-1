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

package l2server.gameserver.handler;

import l2server.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * This class handles all chat handlers
 *
 * @author durgus
 */
public class ChatHandler {
	private static Logger log = LoggerFactory.getLogger(ChatHandler.class.getName());



	private Map<Integer, IChatHandler> datatable = new HashMap<>();

	public static ChatHandler getInstance() {
		return SingletonHolder.instance;
	}

	/**
	 * Singleton constructor
	 */
	private ChatHandler() {
	}

	/**
	 * Register a new chat handler
	 *
	 */
	public void registerChatHandler(IChatHandler handler) {
		int[] ids = handler.getChatTypeList();
		for (int id : ids) {
			if (Config.DEBUG) {
				log.debug("Adding handler for chat type " + id);
			}
			datatable.put(id, handler);
		}
	}

	/**
	 * Get the chat handler for the given chat type
	 *
	 */
	public IChatHandler getChatHandler(int chatType) {
		return datatable.get(chatType);
	}

	/**
	 * Returns the size
	 *
	 */
	public int size() {
		return datatable.size();
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final ChatHandler instance = new ChatHandler();
	}
}
