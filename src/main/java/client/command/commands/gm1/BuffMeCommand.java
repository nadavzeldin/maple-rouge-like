/*
    This file is part of the HeavenMS MapleStory Server, commands OdinMS-based
    Copyleft (L) 2016 - 2019 RonanLana

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/*
   @Author: Arthur L - Refactored command content into modules
*/
package client.command.commands.gm1;

import java.util.List;
import java.util.Map;

import client.Character;
import client.Character.SkillEntry;
import client.Client;
import client.Skill;
import client.SkillFactory;
import client.command.Command;

public class BuffMeCommand extends Command {
    {
        setDescription("Activate GM buffs on self.");
    }
    // all buffs from all jobs
    int[] all_buffs = {1001003,2001003,3001003,1101006,1101007,1201007,1301006,2101001,2201001,2301003,4201003,2111005,2211005,9101002,9101003,9101008,1121000,1121011,1221000,1221012,1321000,1321010,2121000,2121008,2221000,2221008,2321000,2321005,2321009,3121000,3121002,3121009,3221000,3221002,3221008,4121000,4121009,4221000,4221008,5221000,5221010,11101003,15110000,14101003,14111000,12001002,12101000,12101004,13001002,11001001,9001003,9001008,21121000,21121008,21121003, 2121004, 1111002};
    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();
        Map<Skill, SkillEntry> user_skills = player.getSkills();
        List<Integer> user_skills_id = user_skills.keySet().stream().map(skill -> skill.getId()).toList();
        SkillFactory.getSkill(4101004).getEffect(SkillFactory.getSkill(4101004).getMaxLevel()).applyTo(player);
        SkillFactory.getSkill(2311003).getEffect(SkillFactory.getSkill(2311003).getMaxLevel()).applyTo(player);
        SkillFactory.getSkill(1301007).getEffect(SkillFactory.getSkill(1301007).getMaxLevel()).applyTo(player);
        SkillFactory.getSkill(2301004).getEffect(SkillFactory.getSkill(2301004).getMaxLevel()).applyTo(player);
        SkillFactory.getSkill(1005).getEffect(SkillFactory.getSkill(1005).getMaxLevel()).applyTo(player);
        // apply all buffs that the player have
        for (int i = 0; i < all_buffs.length; i++) {
            if (user_skills_id.contains(all_buffs[i])) {
                Skill skill = SkillFactory.getSkill(all_buffs[i]);
                skill.getEffect(skill.getMaxLevel()).applyTo(player);
            }
        }
    }
}
