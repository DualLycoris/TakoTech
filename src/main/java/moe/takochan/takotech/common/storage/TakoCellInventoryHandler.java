package moe.takochan.takotech.common.storage;

import static appeng.util.item.AEItemStackType.ITEM_STACK_TYPE;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import appeng.api.config.IncludeExclude;
import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.storage.ICellCacheRegistry;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.me.storage.MEInventoryHandler;
import appeng.me.storage.MEPassThrough;
import appeng.tile.inventory.IAEStackInventory;
import appeng.util.prioitylist.FuzzyPriorityList;
import appeng.util.prioitylist.IPartitionList;
import appeng.util.prioitylist.OreFilteredList;
import appeng.util.prioitylist.PrecisePriorityList;
import moe.takochan.takotech.utils.OreCellFuzzyPartitionList;

public class TakoCellInventoryHandler extends MEInventoryHandler<IAEItemStack>
    implements ITakoCellInventoryHandler, ICellCacheRegistry {

    public TakoCellInventoryHandler(IMEInventory<IAEItemStack> meInventory) {
        super(meInventory, ITEM_STACK_TYPE);
        final ITakoCellInventory ci = this.getCellInv();

        // init
        if (ci != null) {
            final IInventory upgrades = ci.getUpgradesInventory();
            final String filter = ci.getOreFilter();

            boolean hasInverter = false;
            boolean hasFuzzy = false;
            boolean hasOreFilter = false;
            boolean hasSticky = false;

            for (int i = 0; i < upgrades.getSizeInventory(); i++) {
                final ItemStack is = upgrades.getStackInSlot(i);
                if (is != null && is.getItem() instanceof IUpgradeModule) {
                    final Upgrades u = ((IUpgradeModule) is.getItem()).getType(is);
                    if (u != null) {
                        switch (u) {
                            case FUZZY -> hasFuzzy = true;
                            case INVERTER -> hasInverter = true;
                            case ORE_FILTER -> hasOreFilter = true;
                            case STICKY -> hasSticky = true;
                            default -> {}
                        }
                    }
                }
            }
            this.setWhitelist(hasInverter ? IncludeExclude.BLACKLIST : IncludeExclude.WHITELIST);

            if (hasSticky) {
                setSticky(true);
            }

            if (hasOreFilter && !filter.isEmpty()) {
                this.setPartitionList(new OreFilteredList(filter));
            } else {
                final IAEStackInventory config = ci.getConfigAEInventory();
                final IItemList<IAEItemStack> priorityList = ITEM_STACK_TYPE.createList();
                for (int i = 0; i < config.getSizeInventory(); i++) {
                    if (config.getAEStackInSlot(i) instanceof IAEItemStack stack) {
                        priorityList.add(stack);
                    }
                }
                if (!priorityList.isEmpty()) {
                    if (hasFuzzy) {
                        this.setPartitionList(new OreCellFuzzyPartitionList(priorityList));
                    } else {
                        this.setPartitionList(new PrecisePriorityList<>(priorityList));
                    }
                }
            }
        }
    }

    @Override
    public ITakoCellInventory getCellInv() {
        IMEInventory<?> inventory = this.getInternal();
        if (inventory instanceof MEPassThrough<?>passThrough) {
            inventory = passThrough.getInternal();
        }
        return inventory instanceof ITakoCellInventory cell ? cell : null;
    }

    @Override
    public boolean isFuzzy() {
        IPartitionList<IAEItemStack> partition = this.getPartitionList();
        return partition instanceof OreCellFuzzyPartitionList || partition instanceof FuzzyPriorityList<?>;
    }

    @Override
    public IncludeExclude getIncludeExcludeMode() {
        return this.getWhitelist();
    }

    @Override
    public boolean canGetInv() {
        return this.getCellInv() != null;
    }

    @Override
    public long getTotalBytes() {
        return this.getCellInv()
            .getTotalBytes();
    }

    @Override
    public long getFreeBytes() {
        return this.getCellInv()
            .getFreeBytes();
    }

    @Override
    public long getUsedBytes() {
        return this.getCellInv()
            .getUsedBytes();
    }

    @Override
    public long getTotalTypes() {
        return this.getCellInv()
            .getTotalItemTypes();
    }

    @Override
    public long getFreeTypes() {
        return this.getCellInv()
            .getRemainingItemTypes();
    }

    @Override
    public long getUsedTypes() {
        return this.getCellInv()
            .getStoredItemTypes();
    }

    @Override
    public int getCellStatus() {
        return this.getStatusForCell();
    }

    @Override
    public TYPE getCellType() {
        return TYPE.ITEM;
    }

    public int getStatusForCell() {
        int val = this.getCellInv()
            .getStatusForCell();

        if ((val == 1 || val == 2) && this.isPreformatted()) {
            val = 3;
        }

        return val;
    }
}
