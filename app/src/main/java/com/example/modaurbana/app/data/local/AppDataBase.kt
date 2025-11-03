package com.example.modaurbana.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.modaurbana.app.data.local.dao.CartDao
import com.example.modaurbana.app.data.local.dao.ProductDao
import com.example.modaurbana.app.data.local.dao.UserDao
import com.example.modaurbana.app.data.local.entity.CartItemEntity
import com.example.modaurbana.app.data.local.entity.ProductEntity
import com.example.modaurbana.app.data.local.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Database(
    entities = [ProductEntity::class, UserEntity::class, CartItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun userDao(): UserDao
    abstract fun cartDao(): CartDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "modaurbana_database"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Insertar datos de ejemplo al crear la BD
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    populateDatabase(database)
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateDatabase(database: AppDatabase) {
            val productDao = database.productDao()

            // Productos de ejemplo
            val products = listOf(
                ProductEntity(
                    name = "Polera Urbana Negra",
                    category = "Polera",
                    size = "M",
                    material = "Algodón Reciclado 100%",
                    price = 15990.0,
                    stock = 25,
                    description = "Polera oversized de algodón orgánico con diseño minimalista. Perfecta para el día a día."
                ),
                ProductEntity(
                    name = "Jeans Slim Fit Azul",
                    category = "Pantalón",
                    size = "32",
                    material = "Denim Reciclado",
                    price = 35990.0,
                    stock = 15,
                    description = "Jeans de corte moderno fabricado con materiales reciclados. Cómodo y duradero."
                ),
                ProductEntity(
                    name = "Zapatillas EcoWalk",
                    category = "Zapatillas",
                    size = "42",
                    material = "Caucho Reciclado",
                    price = 45990.0,
                    stock = 10,
                    description = "Zapatillas urbanas con suela de caucho reciclado. Estilo y sostenibilidad en cada paso."
                ),
                ProductEntity(
                    name = "Polera Gráfica Verde",
                    category = "Polera",
                    size = "L",
                    material = "Algodón Orgánico",
                    price = 18990.0,
                    stock = 20,
                    description = "Polera con estampado exclusivo de artista local. 100% algodón orgánico."
                ),
                ProductEntity(
                    name = "Chaqueta Denim",
                    category = "Chaqueta",
                    size = "M",
                    material = "Denim Reciclado",
                    price = 49990.0,
                    stock = 8,
                    description = "Chaqueta clásica de denim reciclado. Perfecta para cualquier ocasión."
                ),
                ProductEntity(
                    name = "Pantalón Cargo Verde",
                    category = "Pantalón",
                    size = "30",
                    material = "Poliéster Reciclado",
                    price = 39990.0,
                    stock = 12,
                    description = "Pantalón cargo con múltiples bolsillos. Funcional y con estilo urbano."
                )
            )

            productDao.insertProducts(products)
        }
    }
}