simple_replace "onBlockPlaced([^a-zA-Z0-9])" "func_85104_a\1"
simple_replace "onPostBlockPlaced" "func_85105_g"
simple_replace "getEntityItem\\(\\)" "item"
simple_replace "detectAndSendChanges" "updateCraftingResults"
simple_replace "itemID" "shiftedIndex"
simple_replace "GameRegistry\\.registerItem[^;]+;" ""
simple_replace "GameRegistry\\.registerBlock *\\(([^,\\(\\)]+),[^,\\(\\)]+\\)" "GameRegistry.registerBlock(\\1)"
simple_replace "GameRegistry\\.registerBlock *\\(([^,\\(\\)]+,[^,\\(\\)]+),[^,\\(\\)]+\\)" "GameRegistry.registerBlock(\\1)"
simple_replace "net\\.minecraft\\.[a-zA-Z0-9\\. ]*\\.([^,\\(\\);\\.]*)" "net.minecraft.src.\\1"
simple_replace "net\\.minecraft\\.src\\.Minecraft([^a-zA-Z0-9])" "net.minecraft.client.Minecraft\\1"
simple_replace "cpw\\.mods\\.fml\\.relauncher\\.SideOnly" "cpw.mods.fml.common.asm.SideOnly"
simple_replace "cpw\\.mods\\.fml\\.relauncher\\.Side" "cpw.mods.fml.common.Side"