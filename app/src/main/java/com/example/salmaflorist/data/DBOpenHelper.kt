package com.example.salmaflorist.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.salmaflorist.model.*

class DBOpenHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "salmaflorist"
        const val DATABASE_VERSION = 8

        // TABLE NAMES
        const val TABLE_CATEGORIES = "categories"
        const val TABLE_ADDRESSES = "addresses"

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

        const val TABLE_USERS = "users"
        const val USER_ID = "id"
        const val USER_NAME = "username"
        const val USER_EMAIL = "email"
        const val USER_PASSWORD = "password"
        const val USER_ROLE = "role"
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
                user_id INTEGER,
                address_id INTEGER,
                invoice_number VARCHAR UNIQUE,
                shipping_number VARCHAR,
                status TEXT,
                total_amount INTEGER,
                shipping_cost INTEGER,
                courier_name VARCHAR,
                courier_code VARCHAR,
                courier_service VARCHAR,
                etd VARCHAR,
                delivery_start_time DATETIME,
                delivery_end_time DATETIME,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (address_id) REFERENCES $TABLE_ADDRESSES(id)
            )
        """.trimIndent()

        // =========================
        // ADDRESSES
        // =========================
        val createAddresses = """
            CREATE TABLE $TABLE_ADDRESSES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                customer_name VARCHAR,
                whatsapp_number VARCHAR,
                address_detail TEXT,
                province_id INTEGER,
                province_name VARCHAR,
                city_id INTEGER,
                city_name VARCHAR,
                district_id INTEGER,
                district_name VARCHAR,
                postal_code VARCHAR
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

        // =========================
        // USERS
        // =========================
        val createUsers = """
            CREATE TABLE $TABLE_USERS (
                $USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $USER_NAME TEXT,
                $USER_EMAIL TEXT UNIQUE,
                $USER_PASSWORD TEXT,
                $USER_ROLE TEXT DEFAULT 'user'
            )
        """.trimIndent()

        db.execSQL(createCategories)
        db.execSQL(createProducts)
        db.execSQL(createCartItems)
        db.execSQL(createAddresses)
        db.execSQL(createOrders)
        db.execSQL(createOrderItems)
        db.execSQL(createUsers)

        seedCategories(db)
        seedProducts(db)
        seedAddresses(db)
        seedOrders(db)
        seedAdmin(db)
    }

    private fun seedAdmin(db: SQLiteDatabase) {
        val values = ContentValues().apply {
            put(USER_NAME, "Admin")
            put(USER_EMAIL, "admin@gmail.com")
            put(USER_PASSWORD, "admin123")
            put(USER_ROLE, "admin")
        }
        db.insert(TABLE_USERS, null, values)
    }

    private fun seedAddresses(db: SQLiteDatabase) {
        val values = ContentValues().apply {
            put("user_id", 0)
            put("customer_name", "User Dummy")
            put("whatsapp_number", "08123456789")
            put("address_detail", "Jl. Mawar No. 123")
            put("province_id", 1)
            put("province_name", "Jawa Barat")
            put("city_id", 1)
            put("city_name", "Bandung")
            put("district_id", 1)
            put("district_name", "Coblong")
            put("postal_code", "40132")
        }
        db.insert(TABLE_ADDRESSES, null, values)
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

    private fun seedOrders(db: SQLiteDatabase) {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        val cal = java.util.Calendar.getInstance()

        val orders = mutableListOf<Triple<String, Int, String>>()
        
        // Orders for today
        orders.add(Triple("INV-${sdf.format(cal.time).replace(":", "").replace("-", "").replace(" ", "")}-001", 150000, "PAID"))
        orders.add(Triple("INV-${sdf.format(cal.time).replace(":", "").replace("-", "").replace(" ", "")}-002", 250000, "PENDING"))
        
        // Orders for last 6 days
        for (i in 1..6) {
            cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            val dateStr = sdf.format(cal.time)
            orders.add(Triple("INV-${dateStr.replace(":", "").replace("-", "").replace(" ", "")}-00${i+2}", 100000 + (i * 50000), "COMPLETED"))
        }

        // Reset calendar for accurate created_at insertion
        cal.time = java.util.Date()

        orders.forEachIndexed { index, (invoice, amount, status) ->
            if (index > 1) cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            
            val values = ContentValues().apply {
                put("user_id", 1)
                put("address_id", 1)
                put("invoice_number", invoice)
                put("shipping_number", "SHIP-${invoice.substringAfterLast("-")}")
                put("status", status)
                put("total_amount", amount)
                put("shipping_cost", 15000)
                put("courier_name", "JNE")
                put("courier_code", "jne")
                put("courier_service", "REG")
                put("etd", "2-3 Hari")
                put("created_at", sdf.format(cal.time))
            }
            val orderId = db.insert(TABLE_ORDERS, null, values)
            
            val itemValues = ContentValues().apply {
                put("product_id", 1)
                put("order_id", orderId)
                put("quantity", 1)
                put("unit_price", amount)
                put("subtotal", amount)
            }
            db.insert(TABLE_ORDER_ITEMS, null, itemValues)
        }
    }


    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ORDER_ITEMS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ORDERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ADDRESSES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CART_ITEMS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")

        onCreate(db)
    }

    // =========================
    // USER METHODS
    // =========================
    fun addUser(username: String, email: String, password: String, role: String = "user"): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(USER_NAME, username)
            put(USER_EMAIL, email)
            put(USER_PASSWORD, password)
            put(USER_ROLE, role)
        }
        val result = db.insert(TABLE_USERS, null, values)
        return result != -1L
    }

    fun checkUser(email: String, password: String): Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $USER_EMAIL = ? AND $USER_PASSWORD = ?"
        val cursor = db.rawQuery(query, arrayOf(email, password))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun getUserRole(email: String): String? {
        val db = readableDatabase
        val query = "SELECT $USER_ROLE FROM $TABLE_USERS WHERE $USER_EMAIL = ?"
        val cursor = db.rawQuery(query, arrayOf(email))
        var role: String? = null
        if (cursor.moveToFirst()) {
            role = cursor.getString(0)
        }
        cursor.close()
        return role
    }

    fun getUserByEmail(email: String): User? {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $USER_EMAIL = ?"
        val cursor = db.rawQuery(query, arrayOf(email))
        var user: User? = null
        if (cursor.moveToFirst()) {
            user = User(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(USER_ID)),
                username = cursor.getString(cursor.getColumnIndexOrThrow(USER_NAME)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(USER_EMAIL))
            )
        }
        cursor.close()
        return user
    }

    fun getAllCategories(): List<Category> {
        val list = mutableListOf<Category>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_CATEGORIES ORDER BY $CAT_NAME ASC", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(Category(cursor.getInt(0), cursor.getString(1)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun addCategory(name: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(CAT_NAME, name)
        }
        val result = db.insert(TABLE_CATEGORIES, null, values)
        return result != -1L
    }

    fun updateCategory(id: Int, newName: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(CAT_NAME, newName)
        }
        val result = db.update(TABLE_CATEGORIES, values, "$CAT_ID = ?", arrayOf(id.toString()))
        return result > 0
    }

    fun addProduct(
        categoryId: Int,
        name: String,
        price: Int,
        description: String,
        weight: Int,
        image: String
    ): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(PROD_CATEGORY_ID, categoryId)
            put(PROD_NAME, name)
            put(PROD_PRICE, price)
            put(PROD_DESCRIPTION, description)
            put(PROD_WEIGHT, weight)
            put(PROD_IMAGE, image)
        }
        val result = db.insert(TABLE_PRODUCTS, null, values)
        return result != -1L
    }

    fun updateProduct(
        id: Int,
        categoryId: Int,
        name: String,
        price: Int,
        description: String,
        weight: Int,
        image: String
    ): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(PROD_CATEGORY_ID, categoryId)
            put(PROD_NAME, name)
            put(PROD_PRICE, price)
            put(PROD_DESCRIPTION, description)
            put(PROD_WEIGHT, weight)
            put(PROD_IMAGE, image)
        }
        val result = db.update(TABLE_PRODUCTS, values, "$PROD_ID = ?", arrayOf(id.toString()))
        return result > 0
    }

    fun deleteProduct(id: Int): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_PRODUCTS, "$PROD_ID = ?", arrayOf(id.toString()))
        return result > 0
    }

    fun getProductById(id: Int): Product? {
        val db = readableDatabase
        val query = """
            SELECT 
                p.$PROD_ID, p.$PROD_CATEGORY_ID, p.$PROD_NAME, p.$PROD_PRICE, 
                p.$PROD_DESCRIPTION, p.$PROD_WEIGHT, p.$PROD_IMAGE,
                c.$CAT_NAME
            FROM $TABLE_PRODUCTS p
            JOIN $TABLE_CATEGORIES c ON p.$PROD_CATEGORY_ID = c.$CAT_ID
            WHERE p.$PROD_ID = ?
        """.trimIndent()
        
        val cursor = db.rawQuery(query, arrayOf(id.toString()))
        var product: Product? = null
        if (cursor.moveToFirst()) {
            val category = Category(cursor.getInt(1), cursor.getString(7))
            product = Product(
                id = cursor.getInt(0),
                categoryId = cursor.getInt(1),
                name = cursor.getString(2),
                price = cursor.getInt(3),
                description = cursor.getString(4),
                weight = cursor.getInt(5),
                image = cursor.getString(6),
                category = category
            )
        }
        cursor.close()
        return product
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
        SELECT 
            c.id, 
            c.product_id, 
            c.quantity,
            p.$PROD_CATEGORY_ID,
            p.$PROD_NAME,
            p.$PROD_PRICE,
            p.$PROD_DESCRIPTION,
            p.$PROD_WEIGHT,
            p.$PROD_IMAGE,
            cat.$CAT_ID,
            cat.$CAT_NAME
        FROM $TABLE_CART_ITEMS c
        JOIN $TABLE_PRODUCTS p ON c.product_id = p.$PROD_ID
        JOIN $TABLE_CATEGORIES cat ON p.$PROD_CATEGORY_ID = cat.$CAT_ID
    """.trimIndent()

        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val category = Category(
                    id = cursor.getInt(9),
                    name = cursor.getString(10)
                )

                val product = Product(
                    id = cursor.getInt(1),
                    categoryId = cursor.getInt(3),
                    name = cursor.getString(4),
                    price = cursor.getInt(5),
                    description = cursor.getString(6),
                    weight = cursor.getInt(7),
                    image = cursor.getString(8),
                    category = category
                )

                list.add(CartItem(
                    cartId = cursor.getInt(0),
                    productId = cursor.getInt(1),
                    quantity = cursor.getInt(2),
                    product = product
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

    // =========================
    // ORDER METHODS
    // =========================
    fun getOrders(statusFilter: String? = null): List<Order> {
        val list = mutableListOf<Order>()
        val db = readableDatabase
        
        var query = "SELECT * FROM $TABLE_ORDERS"
        val args = mutableListOf<String>()
        
        if (!statusFilter.isNullOrEmpty() && statusFilter != "Semua") {
            query += " WHERE status = ?"
            args.add(statusFilter.uppercase())
        }
        
        query += " ORDER BY created_at DESC"
        
        val cursor = db.rawQuery(query, if (args.isEmpty()) null else args.toTypedArray())

        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())

        if (cursor.moveToFirst()) {
            do {
                val statusStr = cursor.getString(cursor.getColumnIndexOrThrow("status"))
                val status = try {
                    OrderStatus.valueOf(statusStr)
                } catch (e: Exception) {
                    OrderStatus.PENDING
                }

                val dateStr = cursor.getString(cursor.getColumnIndexOrThrow("created_at"))
                val date = try {
                    sdf.parse(dateStr) ?: java.util.Date()
                } catch (e: Exception) {
                    java.util.Date()
                }

                list.add(Order(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                    addressId = cursor.getInt(cursor.getColumnIndexOrThrow("address_id")),
                    invoiceNumber = cursor.getString(cursor.getColumnIndexOrThrow("invoice_number")),
                    shippingNumber = cursor.getString(cursor.getColumnIndexOrThrow("shipping_number")) ?: "",
                    status = status,
                    totalAmount = cursor.getInt(cursor.getColumnIndexOrThrow("total_amount")),
                    shippingCost = cursor.getInt(cursor.getColumnIndexOrThrow("shipping_cost")),
                    courierName = cursor.getString(cursor.getColumnIndexOrThrow("courier_name")) ?: "",
                    courierCode = cursor.getString(cursor.getColumnIndexOrThrow("courier_code")) ?: "",
                    courierService = cursor.getString(cursor.getColumnIndexOrThrow("courier_service")) ?: "",
                    etd = cursor.getString(cursor.getColumnIndexOrThrow("etd")) ?: "",
                    deliveryStartTime = null,
                    deliveryEndTime = null,
                    createdAt = date
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getOrderById(orderId: Int): Order? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_ORDERS WHERE id = ?", arrayOf(orderId.toString()))
        var order: Order? = null
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())

        if (cursor.moveToFirst()) {
            val statusStr = cursor.getString(cursor.getColumnIndexOrThrow("status"))
            val status = try { OrderStatus.valueOf(statusStr) } catch (e: Exception) { OrderStatus.PENDING }
            val dateStr = cursor.getString(cursor.getColumnIndexOrThrow("created_at"))
            val date = try { sdf.parse(dateStr) ?: java.util.Date() } catch (e: Exception) { java.util.Date() }

            order = Order(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                addressId = cursor.getInt(cursor.getColumnIndexOrThrow("address_id")),
                invoiceNumber = cursor.getString(cursor.getColumnIndexOrThrow("invoice_number")),
                shippingNumber = cursor.getString(cursor.getColumnIndexOrThrow("shipping_number")) ?: "",
                status = status,
                totalAmount = cursor.getInt(cursor.getColumnIndexOrThrow("total_amount")),
                shippingCost = cursor.getInt(cursor.getColumnIndexOrThrow("shipping_cost")),
                courierName = cursor.getString(cursor.getColumnIndexOrThrow("courier_name")) ?: "",
                courierCode = cursor.getString(cursor.getColumnIndexOrThrow("courier_code")) ?: "",
                courierService = cursor.getString(cursor.getColumnIndexOrThrow("courier_service")) ?: "",
                etd = cursor.getString(cursor.getColumnIndexOrThrow("etd")) ?: "",
                deliveryStartTime = null,
                deliveryEndTime = null,
                createdAt = date
            )
        }
        cursor.close()
        return order
    }

    fun getOrderItems(orderId: Int): List<Pair<OrderItem, Product>> {
        val list = mutableListOf<Pair<OrderItem, Product>>()
        val db = readableDatabase
        val query = """
            SELECT oi.*, p.*, c.name as category_name
            FROM $TABLE_ORDER_ITEMS oi
            JOIN $TABLE_PRODUCTS p ON oi.product_id = p.id
            JOIN $TABLE_CATEGORIES c ON p.category_id = c.id
            WHERE oi.order_id = ?
        """.trimIndent()
        
        val cursor = db.rawQuery(query, arrayOf(orderId.toString()))
        if (cursor.moveToFirst()) {
            do {
                val category = Category(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("category_id")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow("category_name"))
                )
                val product = Product(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("product_id")),
                    categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("category_id")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    price = cursor.getInt(cursor.getColumnIndexOrThrow("price")),
                    description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    weight = cursor.getInt(cursor.getColumnIndexOrThrow("weight")),
                    image = cursor.getString(cursor.getColumnIndexOrThrow("image")),
                    category = category
                )
                val item = OrderItem(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    orderId = cursor.getInt(cursor.getColumnIndexOrThrow("order_id")),
                    productId = cursor.getInt(cursor.getColumnIndexOrThrow("product_id")),
                    quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                    unitPrice = cursor.getInt(cursor.getColumnIndexOrThrow("unit_price")),
                    subTotal = cursor.getInt(cursor.getColumnIndexOrThrow("subtotal"))
                )
                list.add(item to product)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getAddressById(addressId: Int): Address? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_ADDRESSES WHERE id = ?", arrayOf(addressId.toString()))
        var address: Address? = null
        if (cursor.moveToFirst()) {
            address = Address(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                user_id = cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                customerName = cursor.getString(cursor.getColumnIndexOrThrow("customer_name")),
                whatsappNumber = cursor.getLong(cursor.getColumnIndexOrThrow("whatsapp_number")),
                addressDetail = cursor.getString(cursor.getColumnIndexOrThrow("address_detail")),
                provinceId = cursor.getInt(cursor.getColumnIndexOrThrow("province_id")),
                provinceName = cursor.getString(cursor.getColumnIndexOrThrow("province_name")),
                cityid = cursor.getInt(cursor.getColumnIndexOrThrow("city_id")),
                cityName = cursor.getString(cursor.getColumnIndexOrThrow("city_name")),
                districId = cursor.getInt(cursor.getColumnIndexOrThrow("district_id")),
                districName = cursor.getString(cursor.getColumnIndexOrThrow("district_name")),
                postalCode = cursor.getLong(cursor.getColumnIndexOrThrow("postal_code"))
            )
        }
        cursor.close()
        return address
    }

    // =========================
    // DASHBOARD METHODS
    // =========================
    fun getDashboardStats(): Pair<Int, Int> {
        val db = readableDatabase
        var totalOrdersToday = 0
        var totalIncomeToday = 0
        
        val query = """
            SELECT COUNT(*), SUM(total_amount) 
            FROM $TABLE_ORDERS 
            WHERE DATE(created_at) = DATE('now')
            AND status != 'CANCELLED'
        """.trimIndent()
        
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            totalOrdersToday = cursor.getInt(0)
            totalIncomeToday = cursor.getInt(1)
        }
        cursor.close()
        return Pair(totalOrdersToday, totalIncomeToday)
    }

    fun getOrdersLast7Days(): List<Pair<String, Int>> {
        val list = mutableListOf<Pair<String, Int>>()
        val db = readableDatabase
        
        val query = """
            SELECT DATE(created_at) as order_date, COUNT(*) as order_count
            FROM $TABLE_ORDERS
            WHERE created_at >= DATE('now', '-7 days')
            GROUP BY order_date
            ORDER BY order_date ASC
        """.trimIndent()
        
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                list.add(Pair(cursor.getString(0), cursor.getInt(1)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getRecentOrders(limit: Int): List<Order> {
        val list = mutableListOf<Order>()
        val db = readableDatabase
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())

        val query = "SELECT * FROM $TABLE_ORDERS ORDER BY created_at DESC LIMIT ?"
        val cursor = db.rawQuery(query, arrayOf(limit.toString()))

        if (cursor.moveToFirst()) {
            do {
                val statusStr = cursor.getString(cursor.getColumnIndexOrThrow("status"))
                val status = try { OrderStatus.valueOf(statusStr) } catch (e: Exception) { OrderStatus.PENDING }
                val dateStr = cursor.getString(cursor.getColumnIndexOrThrow("created_at"))
                val date = try { sdf.parse(dateStr) ?: java.util.Date() } catch (e: Exception) { java.util.Date() }

                list.add(Order(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                    addressId = cursor.getInt(cursor.getColumnIndexOrThrow("address_id")),
                    invoiceNumber = cursor.getString(cursor.getColumnIndexOrThrow("invoice_number")),
                    shippingNumber = cursor.getString(cursor.getColumnIndexOrThrow("shipping_number")) ?: "",
                    status = status,
                    totalAmount = cursor.getInt(cursor.getColumnIndexOrThrow("total_amount")),
                    shippingCost = cursor.getInt(cursor.getColumnIndexOrThrow("shipping_cost")),
                    courierName = cursor.getString(cursor.getColumnIndexOrThrow("courier_name")) ?: "",
                    courierCode = cursor.getString(cursor.getColumnIndexOrThrow("courier_code")) ?: "",
                    courierService = cursor.getString(cursor.getColumnIndexOrThrow("courier_service")) ?: "",
                    etd = cursor.getString(cursor.getColumnIndexOrThrow("etd")) ?: "",
                    deliveryStartTime = null,
                    deliveryEndTime = null,
                    createdAt = date
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }
}