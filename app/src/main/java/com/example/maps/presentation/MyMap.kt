package com.example.maps.presentation

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.maps.models.LineType
import com.example.maps.utils.bitmapDescriptor
import com.example.maps.utils.capitaliseIt
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.example.maps.R
import com.example.maps.utils.calculateDistance
import com.example.maps.utils.calculateSurfaceArea
import com.example.maps.utils.formattedValue
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.Polyline

@Composable
fun MyMap(
    context: Context,
    latLng: LatLng,
    changeIcon: Boolean = false,
    lineType: LineType? = null,
    mapProperties: MapProperties = MapProperties(),
    onChangeMarkerIcon: () -> Unit,
    onChangeMapType: (mapType: MapType) -> Unit,
    onChangeLineType: (lineType: LineType?) -> Unit
) {
    val latLngList = remember {
        mutableStateListOf(latLng)
    }
    var mapTypeMenuExpanded by remember { mutableStateOf(false) }
    var mapTypeMenuSelectedText by remember {
        mutableStateOf(
            MapType.NORMAL.name.capitaliseIt()//this is the name of the maptype to be shown in the map dropdown
        )
    }
    var lineTypeMenuExpanded by remember { mutableStateOf(false) }
    var lineTypeMenuSelectedText by remember {
        mutableStateOf(
            lineType?.name?.capitaliseIt() ?: "Line Type"
        )
    }
    val cameraPositionState = rememberCameraPositionState{
        position = CameraPosition.fromLatLngZoom(latLng, 15f)
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ){
        GoogleMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            onMapClick = {
                if(lineType == null) {
                    latLngList.add(it) // this will add the marker position latlng list
                }
            }
        ) {
            latLngList.toList().forEach {
                Marker(
                    state = MarkerState(position = it),
                    title = "Location",
                    snippet = "Marker is current location",
                    icon = if(changeIcon){
                        bitmapDescriptor(context, R.drawable.ic_google_maps)
                    } else null
                )
            }
            if (lineType == LineType.POLYLINE) {
                Polyline(points = latLngList, color = Color.Cyan)
            }

            if(lineType == LineType.POLYGON) {
                Polygon(
                    points = latLngList,
                    fillColor = Color.Green,
                    strokeColor = Color.Red,
                    strokeWidth = 2f
                )
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp)
        ){
            Button(
                onClick = onChangeMarkerIcon
            ){
                Text(text = if(changeIcon) "Default Marker" else "Custom Marker")
            }
            Spacer(modifier = Modifier.width(4.dp))
            Row{
               Button(onClick = { mapTypeMenuExpanded = true }) {
                   Text(text = mapTypeMenuSelectedText )
                   Icon(
                       imageVector = Icons.Filled.ArrowDropDown,
                       contentDescription = "Dropdown arrow",
                       modifier = Modifier.size(ButtonDefaults.IconSize)
                   )
               }
                DropdownMenu(
                    expanded = mapTypeMenuExpanded,
                    onDismissRequest = { mapTypeMenuExpanded = false }
                ) {
//                    MapType.values().forEach {
                    MapType.entries.forEach {
                        val mapType = it.name.capitaliseIt()
                        DropdownMenuItem(text = {
                            Text(text = mapType)
                        }, onClick = {
                            onChangeMapType(it)
                            mapTypeMenuSelectedText = mapType
                            mapTypeMenuExpanded = false
                        })
                    }
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
            if(latLngList.size > 1) {
                Row{
                    Button(onClick = { lineTypeMenuExpanded = true }) {
                        Text(text = lineTypeMenuSelectedText )
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Dropdown arrow",
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                    }
                    DropdownMenu(
                        expanded = lineTypeMenuExpanded,
                        onDismissRequest = { lineTypeMenuExpanded = false }
                    ) {
//                    MapType.values().forEach {
                        MapType.entries.forEach {
                            val mapType = it.name.capitaliseIt()
                            DropdownMenuItem(text = {
                                Text(text = mapType)
                            }, onClick = {
                                onChangeMapType(it)
                                lineTypeMenuSelectedText = mapType
                                lineTypeMenuExpanded = false
                            })
                        }
                    }
                    Spacer(modifier = Modifier.padding(4.dp))
                    Button(onClick = { 
                        onChangeLineType(null)
                        latLngList.clear()
                        latLngList.add(latLng)
                    },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text(text = "Clear", color = Color.White)
                    }
                }
            }
        }

        if(lineType != null) {
            Text(
                text = when (lineType) {
                    LineType.POLYLINE -> "Total distance: ${
                        formattedValue(
                            calculateDistance(latLngList)
                        )
                    } km"

                    LineType.POLYGON -> "Total surface area: ${
                        formattedValue(
                            calculateSurfaceArea(latLngList)
                        )
                    } sq. mtrs"
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .background(Color.White)
                    .padding(8.dp),
                fontSize = 20.sp,
                color = Color.Black
            )
        }
    }
}