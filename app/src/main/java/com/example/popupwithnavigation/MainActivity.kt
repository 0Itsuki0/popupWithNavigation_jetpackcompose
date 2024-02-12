package com.example.popupwithnavigation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.popupwithnavigation.ui.theme.PopupWithNavigationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PopupWithNavigationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
//                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun ItemList(items: List<Item>) {
    var showPopup by rememberSaveable { mutableStateOf(false) }
    var selectedItemIndex by rememberSaveable { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly

    ) {
        Spacer(modifier = Modifier)
        items.forEachIndexed { index, item ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(Color.LightGray)
                    .clip(RoundedCornerShape(8.dp))
                    .height(60.dp)
                    .width(200.dp)
                    .clickable {
                        selectedItemIndex = index
                        showPopup = true
                    }
            ) {
                Text(
                    text = item.name
                )
            }
        }
        Spacer(modifier = Modifier)
    }

    if (showPopup) {
        PopupBox(onClickOutside = { showPopup = false }, content = {
            DetailNavHost(
                item = items[selectedItemIndex],
                closePopup = { showPopup = false }
            )
        })
    }
}

@Composable
fun PopupBox(onClickOutside: () -> Unit, content:@Composable () -> Unit) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(red = 0F, green = 0F, blue = 0F, alpha = 0.6F))
            .zIndex(10F),
        contentAlignment = Alignment.Center
    ) {
        // popup
        Popup(
            alignment = Alignment.Center,
            properties = PopupProperties(
                excludeFromSystemGesture = true,
            ),
            // to dismiss on click outside
            onDismissRequest = { onClickOutside() }
        ) {

            content()
        }
    }

}



@Composable
fun DetailNavHost(item: Item, closePopup: () -> Unit) {
    val navController = rememberNavController()

    var canNavigateBack by remember { mutableStateOf(false) }
    navController.addOnDestinationChangedListener{ controller, _, _ ->
        canNavigateBack = controller.previousBackStackEntry != null
    }
    val backStackEntry by navController.currentBackStackEntryAsState()

    val currentScreen = Route.valueOf(
        backStackEntry?.destination?.route ?: Route.Overview.name
    )
    val currentScreenTitle = currentScreen.title

    val config = LocalConfiguration.current
    val screenWidth = config.screenWidthDp.dp


    Box(
        modifier = Modifier
            .clip(shape = RoundedCornerShape(8.dp))
            .height(350.dp)
            .width(screenWidth - 40.dp)
            .background(Color.White)
            .padding(vertical = 15.dp, horizontal = 20.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    title = currentScreenTitle,
                    canNavigateBack = canNavigateBack,
                    onBackButtonPress = {
                        navController.navigateUp()
                    },
                    onCloseButtonPressed = {
                        closePopup()
                        navController.popBackStack(
                            destinationId = navController.graph.findStartDestination().id,
                            inclusive = false
                        )
                    }
                )
            },
            containerColor = Color.White,

            ) {padding->
            NavHost(
                navController = navController,
                startDestination = Route.Overview.name,
                modifier = Modifier
                    .padding(padding)
            ) {
                composable(route = Route.Overview.name) {
                    DetailOverview(
                        navigateTo = { navController.navigate(route = it) }
                    )
                }

                composable(route = Route.Description.name) {
                    Description(item.description)
                }

                composable(route = Route.Comment.name) {
                    Comment(item.comment)
                }
            }
        }
    }
}

@Composable
fun TopBar(title: String, canNavigateBack: Boolean, onBackButtonPress: () -> Unit, onCloseButtonPressed: () -> Unit) {
// title
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        if (canNavigateBack) {
            Image(
                painter = painterResource(id = R.drawable.back),
                contentDescription = "back icon",
                modifier = Modifier
                    .width(35.dp)
                    .height(35.dp)
                    .padding(0.dp, 0.dp, 0.dp, 0.dp)
                    .clickable { onBackButtonPress() }
            )
        } else {
            Spacer(modifier = Modifier.width(30.dp))
        }


        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Image(
            painter = painterResource(id = R.drawable.cancel),
            contentDescription = "cancel icon",
            modifier = Modifier
                .width(35.dp)
                .height(35.dp)
                .padding(0.dp, 0.dp, 0.dp, 0.dp)
                .clickable { onCloseButtonPressed() }
        )
    }
}



@Composable
fun DetailOverview(navigateTo: (route: String) -> Unit) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp, vertical = 15.dp)
    ) {
        // description
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 10.dp)
                .clickable { navigateTo(Route.Description.name) }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(15.dp),
            ) {

                Text(
                    text = Route.Description.title,
                    fontSize = 16.sp,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Left,
                )
            }
            Image(
                painter = painterResource(id = R.drawable.right),
                contentDescription = "right icon",
                modifier = Modifier
                    .width(15.dp)
                    .height(15.dp)
                    .padding(0.dp, 1.dp, 0.dp, 0.dp)
            )
        }
        // comment
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 10.dp)
                .clickable { navigateTo(Route.Comment.name) }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(15.dp),
            ) {

                Text(
                    text = Route.Comment.title,
                    fontSize = 16.sp,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Left,
                )
            }
            Image(
                painter = painterResource(id = R.drawable.right),
                contentDescription = "right icon",
                modifier = Modifier
                    .width(15.dp)
                    .height(15.dp)
                    .padding(0.dp, 1.dp, 0.dp, 0.dp)
            )
        }

    }
}



@Composable
fun Description(description: String) {

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(horizontal = 10.dp, vertical = 15.dp)
            .fillMaxSize()
    ) {
        Text(
            text = description
        )
    }

}

@Composable
fun Comment(comment: String) {
    Box (
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(horizontal = 10.dp, vertical = 15.dp)
            .fillMaxSize()
    ) {
        Text(
            text = comment
        )
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PopupWithNavigationTheme {
        val apple = Item(
            name = "apple",
            description = "I am a green apple",
            comment = "I am great!"
        )
        val banana = Item(
            name = "banana",
            description = "I am a tiny banana",
            comment = "I am sweet!"
        )

        ItemList(items = listOf(apple, banana))
    }
}


enum class Route(val title: String) {
    Overview(title = "Item Details"),
    Description(title = "Description"),
    Comment(title = "Comment"),
}


class Item(name: String, description: String, comment: String) {
    val name = name
    val description = description
    val comment = comment
}