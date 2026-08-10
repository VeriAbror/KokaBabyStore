package hadi.veri.kokababystore.data.repository

import hadi.veri.kokababystore.data.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getProducts(): Flow<List<Product>>
}
