# CunMin18_FAQ1
这是村民拾八视频里的MC模组，您可以在Modrinth下载到它 | [前往下载](https://modrinth.com/project/cunmin18_faq1)  
This is a mod in CunMin18's video, you can download it at Modrinth | [Download](https://modrinth.com/project/cunmin18_faq1)  

<img width="400" height="213" alt="image (2)" src="https://github.com/user-attachments/assets/6df61c35-f3eb-4f9a-a4e7-e82543acc1cd" />  
<img width="400" height="209" alt="image (1)" src="https://github.com/user-attachments/assets/5b1a5a7b-6f70-45fe-b1e3-e3b8ffcec8c5" />  
<img width="400" height="261" alt="image" src="https://github.com/user-attachments/assets/881036e3-9d84-472c-b95d-7fc9b206b173" />  


# 中文简介  
## **村民拾八模组整合（视频第5期）**  

### - 注意：由于视频需要，未考虑可玩性，所以该模组存在BUG多/玩法不完善等问题，且模组本身未来不一定会继续更新，仅供玩家体验。  
### - 目前模组只支持Java Fabric 1.20.4版本。  

    
包含内容:  

1.生物附魔：
- 玩家可以使用附魔书右键对生物进行附魔。
- 目前可用附魔书：效率/时运/忠诚/耐久/火焰附加
- 模组指令
  - /cmfaq1 enchant
    - /cmfaq1 enchant add <type> <level> <targets> 添加附魔
    - /cmfaq1 enchant remove <type> <targets> 移除附魔
      - type:
          - EFFICIENCY 效率
          - FORTUNE 时运
          - UNBREAKING 耐久
          - LOYALTY 忠诚
          - FIRE_ASPECT 火焰附加
          - ABSORBENT 吸水（*无对应附魔书*）
    - /cmfaq1 enchant clear <targets> 清除全部附魔
  - /cmfaq1 rule
    - /cmfaq1 rule <rule> <boolean>
      - rule:
          - enable_loyalty_enchantment 是否忠诚附魔生效
          - isEnable_librarian_task 是否开启图书管理员附魔
          - isEnable_cleric_task 牧师是否丢药水  
    
2.牧师会朝村民/铁傀儡丢药水。  
      
# English 
## **Integration of CunMin18's mod (video phase 5)**

### - Note: Due to the need of video, the playability is not considered, so there are many bugs/imperfect gameplay in this module, and the module itself may not be updated in the future, which is only for players to experience.
### - The current module only supports Java Fabric version 1.20.4.


Contains content:

1.Mob Enchantment
- Player can right click mob entities to enchant them by using enchanted book.
- Enchantable book: EFFICIENCY/FORTUNE/UNBREAKING/LOYALTY/FIRE_ASPECT
- Commands:
  - /cmfaq1 enchant
    - /cmfaq1 enchant add <type> <level> <targets> Add an enchantment to targets.
    - /cmfaq1 enchant remove <type> <targets> Remove an enchantment from targets.
      - type:
          - EFFICIENCY
          - FORTUNE
          - UNBREAKING
          - LOYALTY
          - FIRE_ASPECT
          - ABSORBENT （*No enchanted books*）
    - /cmfaq1 enchant clear <targets> Clear all enchantments
  - /cmfaq1 rule
    - /cmfaq1 rule <rule> <boolean>
      - rule:
          - enable_loyalty_enchantment  
          - isEnable_librarian_task 
          - isEnable_cleric_task   
  
2.The cleric villager will throw potions at the villagers/iron golems.
