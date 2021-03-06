package exnihiloadscensio.registries;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import exnihiloadscensio.config.Config;
import exnihiloadscensio.items.ore.ItemOre;
import exnihiloadscensio.items.ore.Ore;
import exnihiloadscensio.json.CustomBlockInfoJson;
import exnihiloadscensio.json.CustomItemInfoJson;
import exnihiloadscensio.json.CustomOreJson;
import exnihiloadscensio.texturing.Color;
import exnihiloadscensio.util.BlockInfo;
import exnihiloadscensio.util.ItemInfo;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

public class OreRegistry {

	private static List<Ore> registry = new ArrayList<>();
	private static List<Ore> externalRegistry = new ArrayList<>();
	@Getter
	private static HashSet<ItemOre> itemOreRegistry = new HashSet<ItemOre>();

	public static void registerDefaults() {
		registerOre("gold", new Color("FFFF00"), new ItemInfo(Items.GOLD_INGOT, 0));
		registerOre("iron", new Color("BF8040"), new ItemInfo(Items.IRON_INGOT, 0));

		if (OreDictionary.getOres("oreCopper").size() > 0) {
			registerOre("copper", new Color("FF9933"), null);
		}

		if (OreDictionary.getOres("oreTin").size() > 0) {
			registerOre("tin", new Color("E6FFF2"), null);
		}

		if (OreDictionary.getOres("oreAluminium").size() > 0 || OreDictionary.getOres("oreAluminum").size() > 0) {
			registerOre("aluminium", new Color("BFBFBF"), null);
		}

		if (OreDictionary.getOres("oreLead").size() > 0) {
			registerOre("lead", new Color("330066"), null);
		}

		if (OreDictionary.getOres("oreSilver").size() > 0) {
			registerOre("silver", new Color("F2F2F2"), null);
		}

		if (OreDictionary.getOres("oreNickel").size() > 0) {
			registerOre("nickel", new Color("FFFFCC"), null);
		}

		if (OreDictionary.getOres("oreArdite").size() > 0) {
			registerOre("ardite", new Color("FF751A"), null);
		}

		if (OreDictionary.getOres("oreCobalt").size() > 0) {
			registerOre("cobalt", new Color("3333FF"), null);
		}
	}

	// Inconsistency at its finest
	@Deprecated
	/**
	 * Use register instead
	 */
	public static Ore registerOre(String name, Color color, ItemInfo info) {
		return register(name, color, info);
	}

	/**
	 * Registers a new custom piece, hunk, dust and potentially ingot to be
	 * generated by Ex Nihilo Adscensio.
	 * 
	 * @param name
	 *            Unique name for ore
	 * @param color
	 *            Color for the pieces
	 * @param info
	 *            Final result for the process. If null, an ingot is generated.
	 *            Otherwise, the hunk will be smelted into this.
	 * @return Ore, containing the base Ore object.
	 */
	public static Ore register(String name, Color color, ItemInfo info) {
		Ore ore = registerInternal(name, color, info);
		externalRegistry.add(ore);

		return ore;
	}

	/**
	 * Registers a new custom piece, hunk, dust and potentially ingot to be
	 * generated by Ex Nihilo Adscensio.
	 * 
	 * @param name
	 *            Unique name for ore
	 * @param color
	 *            Color for the pieces
	 * @param info
	 *            Final result for the process. If null, an ingot is generated.
	 *            Otherwise, the hunk will be smelted into this.
	 * @return Ore, containing the base Ore object.
	 */
	private static Ore registerInternal(String name, Color color, ItemInfo info) {
		Ore ore = new Ore(name, color, info);
		registry.add(ore);
		itemOreRegistry.add(new ItemOre(ore));

		return ore;
	}

	public static void registerFromRegistry() {
		for (Ore ore : registry) {
			itemOreRegistry.add(new ItemOre(ore));
		}
	}

	public static void doRecipes() {
		for (ItemOre ore : itemOreRegistry) {
			if (Config.shouldOreDictOreChunks)
				OreDictionary.registerOre("ore"+StringUtils.capitalize(ore.getOre().getName()), new ItemStack(ore, 1, 1));
			if (Config.shouldOreDictOreDusts)
				OreDictionary.registerOre("dust"+StringUtils.capitalize(ore.getOre().getName()), new ItemStack(ore, 1, 2));
			GameRegistry.addRecipe(new ItemStack(ore, 1, 1),
					new Object[] { "xx", "xx", 'x', new ItemStack(ore, 1, 0) });

			ItemStack smeltingResult;

			if (ore.isRegisterIngot()) {
				smeltingResult = new ItemStack(ore, 1, 3);
				OreDictionary.registerOre("ingot" + StringUtils.capitalize(ore.getOre().getName()), smeltingResult);
				if (ore.getOre().getName().contains("aluminium"))
					OreDictionary.registerOre("ingotAluminum", smeltingResult);
			} else {
				smeltingResult = ore.getOre().getResult().getItemStack();
			}

			FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(ore, 1, 1), smeltingResult, 0.7f);
			FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(ore, 1, 2), smeltingResult, 0.7f);
		}
	}

	@SideOnly(Side.CLIENT)
	public static void initModels() {
		final ItemMeshDefinition ORES = new ItemMeshDefinition() {
			@Override
			public ModelResourceLocation getModelLocation(ItemStack stack) {
				switch (stack.getItemDamage()) {
				case 0:
					return new ModelResourceLocation("exnihiloadscensio:itemOre", "type=piece");
				case 1:
					return new ModelResourceLocation("exnihiloadscensio:itemOre", "type=hunk");
				case 2:
					return new ModelResourceLocation("exnihiloadscensio:itemOre", "type=dust");
				case 3:
					return new ModelResourceLocation("exnihiloadscensio:itemOre", "type=ingot");
				default:
					return new ModelResourceLocation(stack.getItem().getRegistryName(), "inventory");
				}
			}
		};
		for (ItemOre ore : itemOreRegistry) {
			ModelLoader.setCustomMeshDefinition(ore, ORES);
			ModelBakery.registerItemVariants(ore, new ModelResourceLocation("exnihiloadscensio:itemOre", "type=piece"),
					new ModelResourceLocation("exnihiloadscensio:itemOre", "type=hunk"),
					new ModelResourceLocation("exnihiloadscensio:itemOre", "type=dust"),
					new ModelResourceLocation("exnihiloadscensio:itemOre", "type=ingot"));
			Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(ore, ORES);
		}
	}

	private static Gson gson = new GsonBuilder().setPrettyPrinting()
			.registerTypeAdapter(ItemInfo.class, new CustomItemInfoJson())
			.registerTypeAdapter(BlockInfo.class, new CustomBlockInfoJson())
			.registerTypeAdapter(Ore.class, new CustomOreJson()).create();

	public static void loadJson(File file) {
		registry.clear();
		itemOreRegistry.clear();

		if (file.exists()) {
			try {
				FileReader fr = new FileReader(file);
				List<Ore> gsonInput = gson.fromJson(fr, new TypeToken<List<Ore>>() {
				}.getType());

				registry.addAll(gsonInput);
				registerFromRegistry();
				registry.addAll(externalRegistry);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			registerDefaults();
			// registerDefaults() will add everything to the external registry
			// automatically.
			saveJson(file);
		}
	}

	public static void saveJson(File file) {
		try {
			FileWriter fw = new FileWriter(file);
			gson.toJson(registry, fw);

			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
