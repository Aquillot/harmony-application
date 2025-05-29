package fr.harmony.home

import androidx.paging.PagingSource
import androidx.paging.PagingState
import fr.harmony.database.HarmonizationSessionModelPreview
import fr.harmony.database.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SessionPagingSource(
    private val repo: SessionRepository
) : PagingSource<Int, HarmonizationSessionModelPreview>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, HarmonizationSessionModelPreview> {
        return try {
            val page = params.key ?: 0
            val items = withContext(Dispatchers.IO) {
                repo.loadPreviewPage(page * params.loadSize, params.loadSize)
            }
            LoadResult.Page(
                data     = items,
                prevKey  = if (page == 0) null else page - 1,
                nextKey  = if (items.isEmpty()) null else page + 1
            )
        } catch (e: Throwable) {
            LoadResult.Error(e)
        }
    }


    override fun getRefreshKey(state: PagingState<Int, HarmonizationSessionModelPreview>): Int? {
        return state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }

    }
}
