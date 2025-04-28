package com.example.rickandmorty.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.rickandmorty.model.entity.Person
import com.example.rickandmorty.model.toUsual
import com.example.rickandmorty.ui.viewmodel.ImageViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.io.File


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PersonListScreen() {
    val viewModel: ImageViewModel = viewModel()
    val state by viewModel.uiState.collectAsState() // Используем единое состояние

    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = state.isLoading)

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Rick & Morty",
                        style = MaterialTheme.typography.headlineLarge,
                        fontFamily = FontFamily.Cursive
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(Color(0xff2390b9)),
                scrollBehavior = scrollBehavior
            )
        },
        content = { padding ->
            Box(modifier = Modifier.padding(padding).background(Color(0xffb4e6ed))) {
                when {
                    state.isLoading && state.persons.isEmpty() -> {
                        FullScreenLoading()
                    }
                    state.error != null -> {
                        ErrorMessage(
                            error = state.error!!,
                            onRetry = { viewModel.loadData() }
                        )
                    }
                    state.persons.isEmpty() -> {
                        EmptyState(onRefresh = { viewModel.loadData() })
                    }
                    else -> {
                        SwipeRefresh(
                            state = swipeRefreshState,
                            onRefresh = { viewModel.loadData() }
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                items(state.persons, key = { it.id }) { person ->
                                    PersonListItem(
                                        person = person,
                                        modifier = Modifier
                                            .animateItemPlacement()
                                            .padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (!state.isLoading && state.error == null) {
                FloatingActionButton(
                    onClick = { viewModel.loadData() },
                    containerColor = Color(0xff7573d9)
                ) {
                    Icon(Icons.Default.Refresh, "Обновить")
                }
            }
        }
    )
}

@Composable
fun PersonListItem(person: Person, modifier: Modifier = Modifier) {
    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 16.dp,
            pressedElevation = 8.dp,
            focusedElevation = 24.dp
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp, start = 24.dp, end = 88.dp, bottom = 10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xff6cc8e1))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Блок с изображением
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                if (person.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = Color(0xff7573d9),
                        strokeWidth = 4.dp
                    )
                } else {
                    AsyncImage(
                        model = File(person.filePath),
                        contentDescription = person.name,
                        modifier = Modifier
                            .size(512.dp)
                            .fillMaxWidth(0.8f)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                        error = rememberVectorPainter(Icons.Default.Error),
                        placeholder = rememberVectorPainter(Icons.Default.Image)
                    )
                }
            }

            // Информация о персонаже
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xffdcf0f1))
                    .padding(start = 8.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Имя: ${person.name}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Вид: ${person.species.toUsual()}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Статус: ${person.status.toUsual()}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun FullScreenLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = Color(0xff7573d9),
            strokeWidth = 4.dp
        )
    }
}

@Composable
private fun EmptyState(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Нет данных",
            modifier = Modifier.size(48.dp),
            tint = Color(0xff7573d9)
        )
        Text(
            text = "Нет данных для отображения",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp)
        )
        Button(
            onClick = onRefresh,
            modifier = Modifier.padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xff7573d9))
        ) {
            Text("Попробовать снова")
        }
    }
}

@Composable
private fun ErrorMessage(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Ошибка",
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Text(
            text = error,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
        )
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xff7573d9))
        ) {
            Text("Повторить попытку")
        }
    }
}



/* вариант с маленькими карточками
@Composable
fun PersonListItem(person: Person, modifier: Modifier = Modifier) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xff6cc8e1))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Изображение персонажа
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (person.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xff7573d9),
                        strokeWidth = 3.dp
                    )
                } else {
                    AsyncImage(
                        model = File(person.filePath),
                        contentDescription = person.name,
                        contentScale = ContentScale.Crop,
                        placeholder = rememberVectorPainter(Icons.Default.Person),
                        error = rememberVectorPainter(Icons.Default.BrokenImage),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Информация о персонаже
            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            ) {
                Text(
                    text = person.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${person.status.toUsual()} • ${person.species.toUsual()}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
*/


/*
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PersonListScreen() {
    val viewModel: ImageViewModel = viewModel()
    val persons by viewModel.images.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)

    LaunchedEffect(Unit) {
        viewModel.getImages()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Rick&Morty",
                        style = MaterialTheme.typography.headlineLarge,
                        fontFamily = FontFamily.Cursive
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(Color(0xff2390b9)),
                scrollBehavior = scrollBehavior
            )
        },
        content = { padding ->
            Box(modifier = Modifier.padding(padding)) {
                when {
                    isLoading && persons.isEmpty() -> {
                        // Показываем полностраничный индикатор загрузки при первой загрузке
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    error != null -> {
                        // Показываем сообщение об ошибке
                        ErrorMessage(error = error!!, onRetry = { viewModel.getImages() })
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(persons) { person ->
                                PersonListItem(
                                    person = person,
                                    modifier = Modifier.animateItemPlacement()
                                )
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (!isLoading) {
                FloatingActionButton(
                    onClick = { viewModel.getImages() },
                    containerColor = Color(0xff7573d9)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Обновить"
                    )
                }
            }
        }
    )
}

@Composable
fun PersonListItem(person: Person, modifier: Modifier = Modifier) {
    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 16.dp,
            pressedElevation = 8.dp,
            focusedElevation = 24.dp
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp, start = 24.dp, end = 88.dp, bottom = 10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xff6cc8e1))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Блок с изображением
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                if (person.isLoading) {
                    // Индикатор загрузки для конкретного персонажа
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = Color(0xff7573d9),
                        strokeWidth = 4.dp
                    )
                } else {
                    // Загруженное изображение
                    AsyncImage(
                        model = File(person.filePath),
                        contentDescription = person.name,
                        modifier = Modifier
                            .size(512.dp)
                            .fillMaxWidth(0.8f)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                        error = rememberVectorPainter(Icons.Default.Error), // Добавьте placeholder для ошибок
                        //placeholder = rememberVectorPainter(Icons.Default.Image)// Добавьте placeholder для загрузки
                    )
                }
            }

            // Информация о персонаже
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xffdcf0f1))
                    .padding(start = 8.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Имя: ${person.name}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Вид: ${person.species.toUsual()}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Статус: ${person.status.toUsual()}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun ErrorMessage(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = error,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xff7573d9))
        ) {
            Text("Попробовать снова")
        }
    }
}
*/


/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonListScreen() {
    val viewModel: ImageViewModel = viewModel()
    val persons by viewModel.images.collectAsState()

    val topAppBarState = rememberTopAppBarState()
    LaunchedEffect(Unit) {
        viewModel.getImages()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(
                    text = "Rick&Morty",
                    style = MaterialTheme.typography.headlineLarge,
                    fontFamily = FontFamily.Cursive
                ) },
                colors = TopAppBarDefaults.topAppBarColors(Color(0xff2390b9)), // ff0c5db9
                scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
            )
        },
        content = { padding ->
            LazyColumn (modifier = Modifier.padding(padding)) {
                items(persons) { person ->
                    PersonListItem(person = person)
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.getImages() },
                containerColor = Color(0xff7573d9)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Обновить")
            }
        }
    )
}

@Composable
fun PersonListItem(person: Person) {
    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 16.dp, // Тень в обычном состоянии
            pressedElevation = 8.dp,   // Тень при нажатии (опционально)
            focusedElevation = 24.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, start = 24.dp, end = 88.dp, bottom = 10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xff6cc8e1)) // Синий фон ff3f92d2
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally // Центрируем содержимое по горизонтали
        ) {
            // Картинка с отступами
            Box(
                modifier = Modifier
                    .padding(4.dp) // Отступ от краев Card
                    .fillMaxWidth()
                    .aspectRatio(1f), // Сохраняем пропорции
                contentAlignment = Alignment.Center // Центрируем изображение внутри Box
            ) {
                AsyncImage(
                    model = File(person.filePath),
                    contentDescription = person.name,
                    modifier = Modifier
                        .size(512.dp)
                        .fillMaxWidth(0.8f) // Занимает 80% ширины Box (создает отступы)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            // Текстовый блок, выровненный к началу
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.Start // Текст выровнен по левому краю
            ) {
                Text(
                    text = "Имя: ${person.name}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Вид: ${person.species.toUsual()}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Статус: ${person.status.toUsual()}",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
*/
