package com.nuel.grocers;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.*;
import android.view.animation.AnimationUtils;

public class AppActivity extends AppCompatActivity {

    private RecyclerView productRecyclerView;
    private ProductAdapter productAdapter;
    private ListView cartListView;
    private ArrayAdapter<String> cartAdapter;
    private LinearLayout cartSummary;
    private TextView cartTotalText;
    private FloatingActionButton cartFab;
    private ConstraintLayout mainLayout;
    private SearchView searchView;
    private LinearLayout categoryFilters;

    private List<Product> allProducts;
    private List<Product> cartProducts;
    private List<Product> filteredProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);

        // Initialize data
        allProducts = new ArrayList<>();
        cartProducts = new ArrayList<>();
        filteredProducts = new ArrayList<>();

        initializeProducts();

        // Initialize views
        productRecyclerView = findViewById(R.id.productRecyclerView);
        cartListView = findViewById(R.id.cartListView);
        cartSummary = findViewById(R.id.cartSummary);
        cartTotalText = findViewById(R.id.cartTotalText);
        cartFab = findViewById(R.id.cartFab);
        mainLayout = findViewById(R.id.mainLayout);
        searchView = findViewById(R.id.searchView);
        categoryFilters = findViewById(R.id.categoryFilters);

        // Setup RecyclerView
        productRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        productAdapter = new ProductAdapter();
        productRecyclerView.setAdapter(productAdapter);

        // Setup cart
        cartAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_2, new ArrayList<String>());
        cartListView.setAdapter(cartAdapter);

        // Setup category buttons
        setupCategoryButtons();

        // Set click listeners
        productAdapter.setOnItemClickListener(product -> {
            addToCart(product);
            showNotification(product.name + " added to cart!");
        });

        cartFab.setOnClickListener(v -> {
            if (cartProducts.size() > 0) {
                showCheckoutDialog();
            } else {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
            }
        });

        cartListView.setOnItemClickListener((parent, view, position, id) -> {
            // Long press to remove from cart
        });

        cartListView.setOnLongClickListener(v -> {
            // Show remove option
            return true;
        });

        productAdapter.updateProducts(allProducts);
    }

    private void initializeProducts() {
        allProducts.add(new Product("🍎 Fresh Red Apples", 3.99, "lb", "fruits"));
        allProducts.add(new Product("🥕 Organic Carrots", 2.49, "bunch", "vegetables"));
        allProducts.add(new Product("🥜 Organic Brazil Nuts", 8.99, "pack", "nuts"));
        allProducts.add(new Product("🥚 Free-Range Eggs", 4.99, "dozen", "dairy"));
        allProducts.add(new Product("🫒 Extra Virgin Olive Oil", 12.99, "bottle", "oils"));
        allProducts.add(new Product("🐟 Organic Frozen Salmon", 14.99, "lb", "seafood"));
        allProducts.add(new Product("🥛 Plant-Based Milks", 3.49, "liter", "dairy"));
        allProducts.add(new Product("🍚 Organic Brown Rice", 4.49, "bag", "grains"));
    }

    private void setupCategoryButtons() {
        Map<String, String[]> categories = new LinkedHashMap<String, String>() {{
            put("all", "All");
            put("fruits", "Fruits");
            put("vegetables", "Vegetables");
            put("nuts", "Nuts");
            put("dairy", "Dairy");
            put("oils", "Oils");
            put("seafood", "Seafood");
        }};

        for (String key : categories.keySet()) {
            String categoryName = categories.get(key);
            Button button = new Button(this);
            button.setText(categoryName);
            button.setTag(key);
            button.setTextSize(12);
            button.setPadding(16, 16, 16, 16);
            button.setBackgroundResource(R.drawable.category_button_background);
            button.setTextColor(getResources().getColor(android.R.color.white));

            button.setOnClickListener(v -> {
                String category = (String) v.getTag();
                filterByCategory(category);
                // Highlight selected button
                highlightButton(v);
            });

            categoryFilters.addView(button);
        }
    }

    private void highlightButton(View selected) {
        for (int i = 0; i < categoryFilters.getChildCount(); i++) {
            View child = categoryFilters.getChildAt(i);
            if (child instanceof Button) {
                Button button = (Button) child;
                button.setBackgroundResource(R.drawable.category_button_background);
            }
        }
        selected.setBackgroundResource(R.drawable.category_button_background_selected);
    }

    private void filterByCategory(String category) {
        filteredProducts.clear();
        if (category.equals("all")) {
            filteredProducts.addAll(allProducts);
        } else {
            for (Product p : allProducts) {
                if (p.category.equals(category)) {
                    filteredProducts.add(p);
                }
            }
        }
        productAdapter.updateProducts(filteredProducts);
    }

    private void addToCart(Product product) {
        cartProducts.add(product);
        updateCartUI();
    }

    private void updateCartUI() {
        cartAdapter.clear();
        for (Product p : cartProducts) {
            cartAdapter.add(p.name + "  $" + p.price + "/" + p.unit + "  x1");
        }
        cartAdapter.notifyDataSetChanged();

        cartSummary.setVisibility(View.VISIBLE);
        double total = 0;
        for (Product p : cartProducts) {
            total += p.price;
        }
        cartTotalText.setText("Total: $" + String.format("%.2f", total) + "  (" + cartProducts.size() + " items)");
    }

    private void showNotification(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showCheckoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Checkout");
        builder.setMessage("Total items: " + cartProducts.size() + "\nProceed to checkout?");
        builder.setPositiveButton("Checkout", (dialog, which) -> {
            double total = 0;
            for (Product p : cartProducts) {
                total += p.price;
            }
            Toast.makeText(this, "Order placed! Total: $" + String.format("%.2f", total), Toast.LENGTH_LONG).show();
            cartProducts.clear();
            updateCartUI();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    public static class Product {
        public String name;
        public double price;
        public String unit;
        public String category;

        public Product(String name, double price, String unit, String category) {
            this.name = name;
            this.price = price;
            this.unit = unit;
            this.category = category;
        }
    }
}