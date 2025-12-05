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
                    name = "Polera Heavyweight Tee 'Skysurfer'",
                    category = "Polera",
                    size = "L",
                    material = "Algodón 100%",
                    price = 28990.0,
                    stock = 15,
                    imageUrl = "https://www.stodak.com/cdn/shop/files/DROPANIVERSARIO-04.png?v=1757284633&width=1050",
                    description = "Polera heavyweight oversized con diseño Skysurfer. Confección premium para uso diario."
                ),
                ProductEntity(
                    name = "Polerón Heavyweight 'Skysurfer' Fullzip",
                    category = "Chaqueta",
                    size = "L",
                    material = "Algodón 80% / Poliéster 20%",
                    price = 64990.0,
                    stock = 12,
                    imageUrl = "https://www.stodak.com/cdn/shop/files/DROPANIVERSARIO-03.png?v=1757284679&width=1200",
                    description = "Polerón heavyweight con cierre completo y capucha. Diseño exclusivo Skysurfer con terminaciones de calidad."
                ),
                ProductEntity(
                    name = "Poleron Heavyweight 'Aniversario 5'",
                    category = "Chaqueta",
                    size = "L",
                    material = "Algodón 80% / Poliéster 20%",
                    price = 64990.0,
                    stock = 10,
                    imageUrl = "https://www.stodak.com/cdn/shop/files/DROPANIVERSARIO-10.png?v=1757284664&width=1200",
                    description = "Edición especial Aniversario 5. Polerón heavyweight fullzip con diseño conmemorativo."
                ),
                ProductEntity(
                    name = "Pantalon Flaggy 'Trebol' - Stone Wash",
                    category = "Pantalón",
                    size = "32",
                    material = "Denim 100%",
                    price = 46990.0,
                    stock = 18,
                    imageUrl = "https://cdnx.jumpseller.com/treino/image/67566026/resize/480/480?1757773556",
                    description = "Pantalón denim con lavado stone wash y diseño Trébol. Corte moderno y cómodo."
                ),
                ProductEntity(
                    name = "Buzo Baggy 'Clover Entry' Reflex Negro",
                    category = "Pantalón",
                    size = "L",
                    material = "Algodón 65% / Poliéster 35%",
                    price = 39990.0,
                    stock = 20,
                    imageUrl = "https://cdnx.jumpseller.com/treino/image/63881006/resize/480/480?1748477357",
                    description = "Buzo baggy de corte holgado con detalles reflectivos. Estilo urbano y confortable."
                ),
                ProductEntity(
                    name = "Double Sleeve Tee Oversize - Clover Entry",
                    category = "Polera",
                    size = "L",
                    material = "Algodón 100%",
                    price = 34990.0,
                    stock = 22,
                    imageUrl = "https://cdnx.jumpseller.com/treino/image/67558113/resize/480/480?1757773143",
                    description = "Polera oversize con doble manga y diseño Clover Entry. Fit relajado y moderno."
                ),
                ProductEntity(
                    name = "Chaqueta HUF Crackerjack Baseball - Satin verde",
                    category = "Chaqueta",
                    size = "L",
                    material = "Satín 100%",
                    price = 99990.0,
                    stock = 8,
                    imageUrl = "https://leaked.cl/17682-home_default/chaqueta-huf-crackerjack-baseball-satin-verde.jpg",
                    description = "Chaqueta estilo baseball en satín verde. Diseño HUF Crackerjack con terminaciones premium."
                ),
                ProductEntity(
                    name = "Polera Black Letter Art Co.",
                    category = "Polera",
                    size = "L",
                    material = "Algodón 100%",
                    price = 23990.0,
                    stock = 25,
                    imageUrl = "https://www.inkvasion.cl/cdn/shop/files/POLERA-NERGA-BLACK-LETTER-PECHO-700X980.jpg?v=1752088739&width=660",
                    description = "Polera negra con lettering artístico en el pecho. Diseño minimalista y versátil."
                ),
                ProductEntity(
                    name = "Polera Demon Girl",
                    category = "Polera",
                    size = "L",
                    material = "Algodón 100%",
                    price = 23990.0,
                    stock = 20,
                    imageUrl = "https://www.inkvasion.cl/cdn/shop/files/POLERA-DEMON-GIRL-YOKAI---ACID-WASH_cbc1cfe7-4728-4fc9-af8b-83eb7344e8d4.jpg?v=1744400101&width=660",
                    description = "Polera con diseño Yokai Demon Girl en acid wash. Arte japonés urbano con estilo único."
                ),
                ProductEntity(
                    name = "Ultra Black Baggy Jeans Crystals",
                    category = "Pantalón",
                    size = "32",
                    material = "Denim 98% / Elastano 2%",
                    price = 54990.0,
                    stock = 14,
                    imageUrl = "https://www.stodak.com/cdn/shop/files/DROPANIVERSARIO-05.png?v=1757284566&width=1200",
                    description = "Jeans baggy negro con detalles de cristales. Corte holgado y acabados premium."
                ),
                ProductEntity(
                    name = "Zapatillas New Balance U1906RNB Negro",
                    category = "Zapatilla",
                    size = "42",
                    material = "Malla / Cuero sintético",
                    price = 135990.0,
                    stock = 10,
                    imageUrl = "https://thelinegroupcl.vtexassets.com/arquivos/ids/369682-600-auto?v=638951904106930000&width=600&height=auto&aspect=true",
                    description = "New Balance 1906 en colorway negro. Estilo retro con tecnología de amortiguación moderna."
                ),
                ProductEntity(
                    name = "Zapatillas New Balance 1906ROE Negro",
                    category = "Zapatilla",
                    size = "42",
                    material = "Malla / Cuero sintético",
                    price = 134990.0,
                    stock = 12,
                    imageUrl = "https://thelinegroupcl.vtexassets.com/arquivos/ids/371832-600-auto?v=638959563987100000&width=600&height=auto&aspect=true",
                    description = "New Balance 1906 ROE en negro. Silueta clásica con comfort superior para uso diario."
                ),
                ProductEntity(
                    name = "Poleron Boxy 'Clover Entry' V3 Reflex Gris",
                    category = "Chaqueta",
                    size = "L",
                    material = "Algodón 70% / Poliéster 30%",
                    price = 46990.0,
                    stock = 16,
                    imageUrl = "https://cdnx.jumpseller.com/treino/image/65411013/resize/480/480?1752617437",
                    description = "Polerón boxy fit con detalles reflectivos. Corte oversized en gris con diseño Clover Entry V3."
                ),
                ProductEntity(
                    name = "Poleron Boxy 'Clover Entry' Stone Zip",
                    category = "Chaqueta",
                    size = "L",
                    material = "Algodón 70% / Poliéster 30%",
                    price = 48990.0,
                    stock = 14,
                    imageUrl = "https://cdnx.jumpseller.com/treino/image/65412715/resize/480/480?1752774436",
                    description = "Polerón boxy con cierre stone y capucha. Diseño Clover Entry con fit relajado."
                ),
                ProductEntity(
                    name = "Pantalon Double Knee Denim Pants Light Washed",
                    category = "Pantalón",
                    size = "32",
                    material = "Denim 100%",
                    price = 68990.0,
                    stock = 11,
                    imageUrl = "https://www.nubebrand.cl/cdn/shop/files/92B6A522-10A3-46C8-8DF9-7F9B5EAD452D_1024x1024@2x.jpg?v=1748385843",
                    description = "Pantalón denim con rodillas reforzadas y lavado claro. Estilo workwear con durabilidad extra."
                ),
                ProductEntity(
                    name = "Pantalon Double Knee Denim Pants Washed Black",
                    category = "Pantalón",
                    size = "32",
                    material = "Denim 100%",
                    price = 69990.0,
                    stock = 13,
                    imageUrl = "https://www.nubebrand.cl/cdn/shop/files/DFB74D11-EDAB-41FA-94CE-9196CCB3F4E8_1024x1024@2x.png?v=1748385940",
                    description = "Pantalón denim negro con rodillas reforzadas. Construcción resistente de estilo workwear."
                ),
                ProductEntity(
                    name = "Polera NMS Fake Long Sleeve Washed Tee",
                    category = "Polera",
                    size = "L",
                    material = "Algodón 100%",
                    price = 27990.0,
                    stock = 18,
                    imageUrl = "https://www.nubebrand.cl/cdn/shop/files/mangalargawalkman_1024x1024@2x.png?v=1754524665",
                    description = "Polera manga larga con efecto fake sleeve y lavado vintage. Diseño NMS con estilo único."
                ),
                ProductEntity(
                    name = "Zapatillas Jordan Spizike Low",
                    category = "Zapatilla",
                    size = "42",
                    material = "Cuero / Textil",
                    price = 172990.0,
                    stock = 8,
                    imageUrl = "https://thelinegroupcl.vtexassets.com/arquivos/ids/328728-600-auto?v=638821388045770000&width=600&height=auto&aspect=true",
                    description = "Jordan Spizike Low combinando elementos icónicos de modelos clásicos. Estilo retro premium."
                ),
                ProductEntity(
                    name = "Zapatillas Jordan Spizike Low",
                    category = "Zapatilla",
                    size = "42",
                    material = "Cuero / Textil",
                    price = 172990.0,
                    stock = 9,
                    imageUrl = "https://thelinegroupcl.vtexassets.com/arquivos/ids/328673-600-auto?v=638821387025570000&width=600&height=auto&aspect=true",
                    description = "Jordan Spizike Low con diseño híbrido icónico. Fusión perfecta de estilo y performance."
                ),
                ProductEntity(
                    name = "Polera The Lovers Tee",
                    category = "Polera",
                    size = "L",
                    material = "Algodón 100%",
                    price = 23990.0,
                    stock = 22,
                    imageUrl = "https://www.inkvasion.cl/cdn/shop/files/POLERA-NEGRA-THE-LOVERS-ESPALDA-700X980_abb5b616-1247-4c27-a7c1-3f90d107df52.jpg?v=1737392065&width=660",
                    description = "Polera negra con diseño The Lovers en espalda. Gráfica artística inspirada en tarot."
                ),
                ProductEntity(
                    name = "Polera Inkvasion Art Co. Japo Style",
                    category = "Polera",
                    size = "L",
                    material = "Algodón 100%",
                    price = 23990.0,
                    stock = 20,
                    imageUrl = "https://www.inkvasion.cl/cdn/shop/files/POLERA-NEGRA-JAPO-LETTERS-700X980_cc010722-80d7-4c0b-ac57-b88c5e98ce17.jpg?v=1737392238&width=660",
                    description = "Polera con lettering japonés estilizado. Diseño Inkvasion Art Co. con influencia asiática."
                ),
                ProductEntity(
                    name = "Poleron Big Stitch Hoodie Red",
                    category = "Chaqueta",
                    size = "L",
                    material = "Algodón 80% / Poliéster 20%",
                    price = 40000.0,
                    stock = 15,
                    imageUrl = "https://www.nubebrand.cl/cdn/shop/files/5633B04E-733C-414E-8C8F-1CCD448A19BC_1024x1024@2x.jpg?v=1759610461",
                    description = "Polerón rojo con costuras grandes decorativas. Diseño Nube con detalles bold stitch."
                )
            )
            productDao.insertProducts(products)
        }
    }
}