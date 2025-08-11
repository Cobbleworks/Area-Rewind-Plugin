package arearewind.gui.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for paginated GUI content
 */
public class PaginatedContent<T> {
    private final List<T> items;
    private final int itemsPerPage;
    private int currentPage;

    public PaginatedContent(List<T> items, int itemsPerPage) {
        this.items = new ArrayList<>(items);
        this.itemsPerPage = itemsPerPage;
        this.currentPage = 0;
    }

    public int getItemsPerPage() {
        return itemsPerPage;
    }

    public List<T> getCurrentPageItems() {
        int start = currentPage * itemsPerPage;
        int end = Math.min(start + itemsPerPage, items.size());

        if (start >= items.size()) {
            return new ArrayList<>();
        }

        return new ArrayList<>(items.subList(start, end));
    }

    public boolean hasNextPage() {
        return (currentPage + 1) * itemsPerPage < items.size();
    }

    public boolean hasPreviousPage() {
        return currentPage > 0;
    }

    public void nextPage() {
        if (hasNextPage()) {
            currentPage++;
        }
    }

    public void previousPage() {
        if (hasPreviousPage()) {
            currentPage--;
        }
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getTotalPages() {
        return (int) Math.ceil((double) items.size() / itemsPerPage);
    }

    public int getTotalItems() {
        return items.size();
    }

    public void setPage(int page) {
        if (page >= 0 && page < getTotalPages()) {
            this.currentPage = page;
        }
    }

    public String getPageInfo() {
        if (items.isEmpty()) {
            return "No items";
        }
        return "Page " + (currentPage + 1) + " of " + getTotalPages();
    }

    /**
     * Add pagination controls to a GUI
     */
    public static void addPaginationControls(BaseGUI gui, PaginatedContent<?> pagination,
            int prevSlot, int nextSlot, int infoSlot) {
        // Previous page button
        if (pagination.hasPreviousPage()) {
            gui.getInventory().setItem(prevSlot, ItemBuilder.createPrevPageButton());
            gui.setAction(prevSlot, (player, event) -> {
                pagination.previousPage();
                gui.refresh();
            });
        }

        // Next page button
        if (pagination.hasNextPage()) {
            gui.getInventory().setItem(nextSlot, ItemBuilder.createNextPageButton());
            gui.setAction(nextSlot, (player, event) -> {
                pagination.nextPage();
                gui.refresh();
            });
        }

        // Page info
        if (infoSlot >= 0) {
            ItemStack pageInfo = ItemBuilder.createInfoItem("Page Information",
                    "&7" + pagination.getPageInfo(),
                    "&7Total Items: " + pagination.getTotalItems());
            gui.getInventory().setItem(infoSlot, pageInfo);
        }
    }
}
