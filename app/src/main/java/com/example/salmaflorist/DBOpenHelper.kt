package com.example.salmaflorist

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBOpenHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "salmaflorist"
        const val DATABASE_VERSION = 1

        // TABLE NAMES
        const val TABLE_CATEGORIES = "categories"

        const val CAT_ID = "id"
        const val CAT_NAME = "name"

        const val TABLE_PRODUCTS = "products"

        const val PROD_ID = "id"
        const val PROD_CATEGORY_ID = "category_id"
        const val PROD_NAME = "name"
        const val PROD_PRICE = "price"
        const val PROD_DESCRIPTION = "description"
        const val PROD_WEIGHT = "weight"
        const val PROD_IMAGE = "image"

        const val TABLE_CART_ITEMS = "cart_items"
        const val TABLE_ORDERS = "orders"
        const val TABLE_ORDER_ITEMS = "order_items"
    }

    override fun onCreate(db: SQLiteDatabase) {

        db.execSQL("PRAGMA foreign_keys=ON")

        // =========================
        // CATEGORIES
        // =========================
        val createCategories = """
            CREATE TABLE $TABLE_CATEGORIES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name VARCHAR
            )
        """.trimIndent()

        // =========================
        // PRODUCTS
        // =========================
        val createProducts = """
            CREATE TABLE $TABLE_PRODUCTS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                category_id INTEGER,
                name VARCHAR,
                price INTEGER,
                weight INTERGER,
                description TEXT,
                image VARCHAR,
                FOREIGN KEY (category_id)
                    REFERENCES $TABLE_CATEGORIES(id)
                    ON DELETE CASCADE
            )
        """.trimIndent()

        // =========================
        // CART ITEMS
        // =========================
        val createCartItems = """
            CREATE TABLE $TABLE_CART_ITEMS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                product_id INTEGER,
                quantity INTEGER,
                FOREIGN KEY (product_id)
                    REFERENCES $TABLE_PRODUCTS(id)
                    ON DELETE CASCADE
            )
        """.trimIndent()

        // =========================
        // ORDERS
        // =========================
        val createOrders = """
            CREATE TABLE $TABLE_ORDERS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                invoice_number VARCHAR UNIQUE,
                customer_name VARCHAR,
                whatsapp_number VARCHAR,
                address_detail TEXT,
                total_amount INTEGER,
                status TEXT,
                shipping_method TEXT,
                notes TEXT,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent()

        // =========================
        // ORDER ITEMS
        // =========================
        val createOrderItems = """
            CREATE TABLE $TABLE_ORDER_ITEMS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                product_id INTEGER,
                order_id INTEGER,
                quantity INTEGER,
                unit_price INTEGER,
                subtotal INTEGER,
                FOREIGN KEY (product_id)
                    REFERENCES $TABLE_PRODUCTS(id)
                    ON DELETE CASCADE,
                FOREIGN KEY (order_id)
                    REFERENCES $TABLE_ORDERS(id)
                    ON DELETE CASCADE
            )
        """.trimIndent()

        db.execSQL(createCategories)
        db.execSQL(createProducts)
        db.execSQL(createCartItems)
        db.execSQL(createOrders)
        db.execSQL(createOrderItems)

        seedCategories(db)
        seedProducts(db)
    }

    private fun seedCategories(db: SQLiteDatabase) {

        val categories = listOf(
            "Bucket",
            "Papan bunga",
            "Bunga meja"
        )

        for (name in categories) {

            val values = ContentValues()

            values.put(CAT_NAME, name)

            db.insert(TABLE_CATEGORIES, null, values)
        }
    }

    private fun seedProducts(db: SQLiteDatabase) {

        insertProduct(
            db,
            2,
            "Papan Bunga Ucapan Selamat",
            550000,
            "Papan bunga warna cerah dengan tulisan selamat.",
            5000,
            "bunga1"
        )

        insertProduct(
            db,
            1,
            "Buket Campuran Pastel",
            270000,
            "Campuran bunga warna pastel seperti pink, peach, dan putih.",
            1000,
            "bunga2"
        )

        insertProduct(
            db,
            3,
            "Bunga Meja Lily Putih",
            280000,
            "Arrangement bunga lily putih dalam vas kaca.",
            2000,
            "bunga3"
        )

        insertProduct(
            db,
            1,
            "Bouquet Tulip Putih Elegant",
            320000,
            "Bouquet berisi 10 tulip putih impor.",
            1000,
            "bunga4"
        )

        insertProduct(
            db,
            1,
            "Bouquet Mawar Merah Premium",
            350000,
            "Bouquet elegan berisi 12 mawar merah premium.",
            1200,
            "bunga5"
        )
    }

    private fun insertProduct(
        db: SQLiteDatabase,
        categoryId: Int,
        name: String,
        price: Int,
        description: String,
        weight: Int,
        image: String
    ) {

        val values = ContentValues()

        values.put(PROD_CATEGORY_ID, categoryId)

        values.put(PROD_NAME, name)

        values.put(PROD_PRICE, price)

        values.put(PROD_DESCRIPTION, description)

        values.put(PROD_WEIGHT, weight)

        values.put(PROD_IMAGE, image)

        db.insert(TABLE_PRODUCTS, null, values)
    }


    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ORDER_ITEMS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ORDERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CART_ITEMS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")

        onCreate(db)
    }

    fun getAllCategories(): List<Category> {
        val list = mutableListOf<Category>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_CATEGORIES", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(Category(cursor.getInt(0), cursor.getString(1)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getTopProducts(): List<Product> {
        val list = ArrayList<Product>()

        val db = readableDatabase

        var query = """
        SELECT 
            p.$PROD_ID,
            p.$PROD_CATEGORY_ID,
            p.$PROD_NAME,
            p.$PROD_PRICE,
            p.$PROD_DESCRIPTION,
            p.$PROD_WEIGHT,
            p.$PROD_IMAGE,
            c.$CAT_ID,
            c.$CAT_NAME
        FROM $TABLE_PRODUCTS p
        INNER JOIN $TABLE_CATEGORIES c
        ON p.$PROD_CATEGORY_ID = c.$CAT_ID
        LIMIT 4
    """.trimIndent()

        val cursor = db.rawQuery(
            query,
            null
        )

        if (cursor.moveToFirst()) {

            do {

                val category = Category(

                    id = cursor.getInt(7),

                    name = cursor.getString(8)

                )

                val product = Product(

                    id = cursor.getInt(0),

                    categoryId = cursor.getInt(1),

                    name = cursor.getString(2),

                    price = cursor.getInt(3),

                    description = cursor.getString(4),

                    weight = cursor.getInt(5),

                    image = cursor.getString(6),

                    category = category

                )

                list.add(product)

            } while (cursor.moveToNext())
        }

        cursor.close()

        return list
    }

    fun getProducts(
        keyword: String,
        kategori: String,
        sort: String
    ): ArrayList<Product> {

        val list = ArrayList<Product>()

        val db = readableDatabase

        val args = ArrayList<String>()

        var query = """
        SELECT 
            p.$PROD_ID,
            p.$PROD_CATEGORY_ID,
            p.$PROD_NAME,
            p.$PROD_PRICE,
            p.$PROD_DESCRIPTION,
            p.$PROD_WEIGHT,
            p.$PROD_IMAGE,
            c.$CAT_ID,
            c.$CAT_NAME
        FROM $TABLE_PRODUCTS p
        INNER JOIN $TABLE_CATEGORIES c
        ON p.$PROD_CATEGORY_ID = c.$CAT_ID
        WHERE p.$PROD_NAME LIKE ?
    """.trimIndent()

        args.add("%$keyword%")

        // FILTER CATEGORY
        if (kategori != "all") {

            query += " AND p.$PROD_CATEGORY_ID = ?"

            args.add(kategori)
        }

        // SORT
        when (sort) {

            "Harga Terendah" ->
                query += " ORDER BY p.$PROD_PRICE ASC"

            "Harga Tertinggi" ->
                query += " ORDER BY p.$PROD_PRICE DESC"
        }

        val cursor = db.rawQuery(
            query,
            args.toTypedArray()
        )

        if (cursor.moveToFirst()) {

            do {

                val category = Category(

                    id = cursor.getInt(7),

                    name = cursor.getString(8)

                )

                val product = Product(

                    id = cursor.getInt(0),

                    categoryId = cursor.getInt(1),

                    name = cursor.getString(2),

                    price = cursor.getInt(3),

                    description = cursor.getString(4),

                    weight = cursor.getInt(5),

                    image = cursor.getString(6),

                    category = category

                )

                list.add(product)

            } while (cursor.moveToNext())
        }

        cursor.close()

        return list
    }

    fun addToCart(productId: Int, quantity: Int = 1) {
        val db = writableDatabase

        // Cek apakah produk sudah ada di keranjang
        val cursor = db.rawQuery(
            "SELECT $PROD_ID, quantity FROM $TABLE_CART_ITEMS WHERE product_id = ?",
            arrayOf(productId.toString())
        )

        if (cursor.moveToFirst()) {
            // Jika sudah ada, update quantity (Quantity + 1)
            val currentQty = cursor.getInt(1)
            val values = ContentValues().apply {
                put("quantity", currentQty + quantity)
            }
            db.update(TABLE_CART_ITEMS, values, "product_id = ?", arrayOf(productId.toString()))
        } else {
            // Jika belum ada, insert baru
            val values = ContentValues().apply {
                put("product_id", productId)
                put("quantity", quantity)
            }
            db.insert(TABLE_CART_ITEMS, null, values)
        }
        cursor.close()
    }

    fun getCartItems(): ArrayList<CartItem> {
        val list = ArrayList<CartItem>()
        val db = readableDatabase
        val query = """
        SELECT c.id, c.product_id, p.$PROD_NAME, p.$PROD_PRICE, p.$PROD_IMAGE, c.quantity
        FROM $TABLE_CART_ITEMS c
        JOIN $TABLE_PRODUCTS p ON c.product_id = p.$PROD_ID
    """.trimIndent()

        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                list.add(CartItem(
                    cursor.getInt(0), cursor.getInt(1), cursor.getString(2),
                    cursor.getInt(3), cursor.getString(4), cursor.getInt(5)
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    // Update quantity
    fun updateCartQuantity(cartId: Int, newQty: Int) {
        val db = writableDatabase
        if (newQty > 0) {
            val values = ContentValues().apply { put("quantity", newQty) }
            db.update(TABLE_CART_ITEMS, values, "id = ?", arrayOf(cartId.toString()))
        } else {
            deleteCartItem(cartId)
        }
    }

    // Hapus item
    fun deleteCartItem(cartId: Int) {
        val db = writableDatabase
        db.delete(TABLE_CART_ITEMS, "id = ?", arrayOf(cartId.toString()))
    }
}