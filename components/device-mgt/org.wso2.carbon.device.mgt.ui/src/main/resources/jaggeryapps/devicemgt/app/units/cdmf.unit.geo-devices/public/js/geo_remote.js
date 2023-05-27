/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/* All the remote calls to backend server*/

/* close all opened modals and side pane */
function addTileUrl() {

    var tileUrl = $('#tileUrl').val();
    var urlName = $('#tileName').val();
    var maxzoom = $('#maxzoom').val();
    subdomains = $('#sub_domains').val();
    var attribution = $('#data_attribution').val();

    /* Add to base layers*/
    var newTileLayer = L.tileLayer(tileUrl, {
        maxZoom: parseInt(maxzoom),
        attribution: attribution
    });
    layerControl.addBaseLayer(newTileLayer, urlName);

    inputs = layerControl._form.getElementsByTagName('input');
    inputsLen = inputs.length;
    for (i = 0; i < inputsLen; i++) {
        input = inputs[i];
        obj = layerControl._layers[input.layerId];
        if (layerControl._map.hasLayer(obj.layer)) {
            map.removeLayer(obj.layer);
        }
    }
    map.addLayer(newTileLayer);

    /* Do ajax save */
    var data = {
        url: tileUrl,
        'name': urlName,
        'attribution': attribution,
        'maxzoom': maxzoom,
        'subdomains': subdomains
    };
    var serverUrl = "/portal/store/carbon.super/fs/gadget/geo-dashboard/controllers/tile_servers.jag";
    // TODO: If failure happens notify user about the error message
    $.post(serverUrl, data, function (response) {
        noty({text: '<span style="color: dodgerblue">' + response + '</span>', type: 'success' });
        closeAll();
    });
}

var defaultOSM = L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19
});

var baseLayers = {
    "Open Street Maps": defaultOSM
};

function getTileServers() {
    /*var backendApiUrl = $("#arduino-div-chart").data("backend-api-url") + "?from=" + from + "&to=" + to;
     invokerUtil.get(backendApiUrl, successCallback, function (message) {

     });*/
    $.getJSON("/api/controllers/tile_servers?serverId=all", function (data) {
        console.log(JSON.stringify(data));
        $.each(data, function (key, val) {
            noty({text: 'Loading... <span style="color: #ccfcff">' + val.NAME + '</span>', type: 'info'});
            //baseLayers[val.name]
            var newTileLayer = L.tileLayer(
                val.URL, {
                    maxZoom: val.MAXZOOM, // TODO: if no maxzoom level do not set this attribute
                    attribution: val.ATTRIBUTION
                }
            );
            layerControl.addBaseLayer(newTileLayer, val.NAME); // TODO: implement single method for #20  and this and do validation
            //map.addLayer(newTileLayer);
        });
    });
}

function addWmsEndPoint() {

    serviceName = $('#serviceName').val();
    layers = $('#layers').val();
    wmsVersion = $('#wmsVersion').val();
    serviceEndPoint = $('#serviceEndPoint').val();
    outputFormat = $('#outputFormat').val();

    var validated = false;

    if (serviceName === undefined || serviceName == "" || serviceName == null) {
        var message = "Service Provider name cannot be empty.";
        noty({text: '<span style="color: red">' + message + '</span>', type: 'error'});
    }

    if (validated) {
        wmsLayer = L.tileLayer.wms(serviceEndPoint, {
            layers: layers.split(','),
            format: outputFormat ? outputFormat : 'image/png',
            version: wmsVersion,
            transparent: true,
            opacity: 0.4
        });

        layerControl.addOverlay(wmsLayer, serviceName, "Web Map Service layers");
        map.addLayer(wmsLayer);

        var data = {
            'serviceName': serviceName,
            'layers': layers,
            'wmsVersion': wmsVersion,
            'serviceEndPoint': serviceEndPoint,
            'outputFormat': outputFormat
        };
        var serverUrl = "/api/controllers/wms_endpoints";
        // TODO: If failure happens notify user about the error message
        $.post(serverUrl, data, function (response) {
            noty({
                text: '<span style="color: dodgerblue">' + response + '</span>',
                type: 'success'
            });
            closeAll();
        });
    }


}

function loadWms() {
    // For refference {"wmsServerId" : 1, "serviceUrl" : "http://{s}.somedomain.com/blabla/{z}/{x}/{y}.png", "name" : "Sample server URL", "layers" : "asdsad,sd,adasd,asd", "version" : "1.0.2", "format" : "sadasda/asdas"}
    $.getJSON("/api/controllers/wms_endpoints?serverId=all", function (data) {
        $.each(data, function (key, val) {

            wmsLayer = L.tileLayer.wms(val.SERVICEURL, {
                layers: val.LAYERS.split(','),
                format: val.FORMAT ? val.FORMAT : 'image/png',
                version: val.VERSION,
                transparent: true,
                opacity: 0.4
            });
            layerControl.addOverlay(wmsLayer, val.NAME, "Web Map Service layers");
        });
    });
}

function setSpeedAlert() {
    //TODO: get the device Id from the URL
    var speedAlertValue = $("#speedAlertValue").val();

    if (!speedAlertValue) {
        var message = "Speed cannot be empty.";
        noty({text: message, type: 'error'});
    } else {
        data = {
            'parseData': JSON.stringify({
                'speedAlertValue': speedAlertValue,
                'deviceId': deviceId}), // parseKey : parseValue pair , this key pair is replace with the key in the template file
            'executionPlan': 'Speed',
            'customName': null,
            'cepAction': 'edit',
            'deviceId': deviceId
        };
        var serviceUrl = '/api/device-mgt/v1.0/geo-services/alerts/Speed';
        var responseHandler = function (data, textStatus, xhr) {
            closeAll();
            if (xhr.status == 200) {
                noty({text: 'Successfully added alert', type: 'success'});
            } else {
                var ptrn = /(?:<am\:description>)(.*)(?:<\/am\:description>)/g;
                var result = (ptrn.exec(data));
                var errorTxt;
                if (result) {
                    errorTxt = result.length > 1 ? result[1] : data;
                } else {
                    errorTxt = data;
                }
                noty({text: textStatus + ' : ' + errorTxt, type: 'error'});
            }
        };
        invokerUtil.put(serviceUrl,
            data,
            responseHandler, function (xhr) {
                responseHandler(xhr.responseText, xhr.statusText, xhr);
            });
    }


}
var lastToolLeafletId = null;

function setWithinAlert(leafletId) {
    /*
     * TODO: replace double quote to single quote because of a conflict when deploying execution plan in CEP
     * this is against JSON standards so has been re-replaced when getting the data from governance registry
     * (look in get_alerts for .replace() method)
     * */
    var selectedAreaGeoJson = JSON.stringify(map._layers[leafletId].toGeoJSON().geometry).replace(/"/g, "'");
    var areaName = $("#withinAlertAreaName").val();
    var queryName = areaName;


    if (areaName == null || areaName === undefined || areaName == "") {
        var message = "Area Name cannot be empty.";
        noty({text: message, type: 'error'});
    } else if ($.trim(areaName).indexOf(" ") > -1) {
        var message = "Area Name cannot contain spaces.";
        noty({text: message, type: 'error'});
    } else {
        areaName = $.trim(areaName);
        var data = {
            'parseData': JSON.stringify({
                'geoFenceGeoJSON': selectedAreaGeoJson,
                'areaName': areaName,
                'deviceId': deviceId
            }),
            'executionPlan': 'Within',
            'customName': areaName, // TODO: fix , When template copies there can be two queryName and areaName id elements in the DOM
            'queryName': queryName,
            'cepAction': 'deploy',
            'deviceId': deviceId
        };

        var serviceUrl = '/api/device-mgt/v1.0/geo-services/alerts/Within';
        var responseHandler = function (data, textStatus, xhr) {
            closeTools(leafletId);
            if (xhr.status == 200) {
                noty({text: 'Successfully added alert', type: 'success'});
            } else {
                var ptrn = /(?:<am\:description>)(.*)(?:<\/am\:description>)/g;
                var errorTxt;
                var result = (ptrn.exec(data));
                if (result) {
                    errorTxt = result.length > 1 ? result[1] : data;
                } else {
                    errorTxt = data;
                }
                noty({text: textStatus + ' : ' + errorTxt, type: 'error'});
            }
        };
        invokerUtil.post(serviceUrl,
            data,
            responseHandler, function (xhr) {
                responseHandler(xhr.responseText, xhr.statusText, xhr);
            });
        viewFenceByData(selectedAreaGeoJson, queryName, areaName, null, 'WithIn');
    }
}

function setExitAlert(leafletId) {
    /*
     * TODO: replace double quote to single quote because of a conflict when deploying execution plan in CEP
     * this is against JSON standards so has been re-replaced when getting the data from governance registry
     * (look in get_alerts for .replace() method)
     * */
    var selectedAreaGeoJson = JSON.stringify(map._layers[leafletId].toGeoJSON().geometry).replace(/"/g, "'");
    var areaName = $("#exitAlertAreaName").val();
    var queryName = areaName;


    if (areaName == null || areaName === undefined || areaName == "") {
        var message = "Area Name cannot be empty.";
        noty({text: message, type: 'error'});
    } else if ($.trim(areaName).indexOf(" ") > -1) {
        var message = "Area Name cannot contain spaces.";
        noty({text: message, type: 'error'});
    } else {
        areaName = $.trim(areaName);
        var data = {
            'parseData': JSON.stringify({
                'geoFenceGeoJSON': selectedAreaGeoJson,
                'areaName': areaName,
                'deviceId': deviceId
            }),
            'executionPlan': 'Exit',
            'customName': areaName, // TODO: fix , When template copies there can be two queryName and areaName id elements in the DOM
            'queryName': queryName,
            'cepAction': 'deploy',
            'deviceId': deviceId
        };

        var serviceUrl = '/api/device-mgt/v1.0/geo-services/alerts/Exit';
        var responseHandler = function (data, textStatus, xhr) {
            closeTools(leafletId);
            if (xhr.status == 200) {
                noty({text: 'Successfully added alert', type: 'success'});
            } else {
                var ptrn = /(?:<am\:description>)(.*)(?:<\/am\:description>)/g;
                var errorTxt;
                var result = (ptrn.exec(data));
                if (result) {
                    errorTxt = result.length > 1 ? result[1] : data;
                } else {
                    errorTxt = data;
                }
                noty({text: textStatus + ' : ' + errorTxt, type: 'error'});
            }
        };
        invokerUtil.post(serviceUrl,
            data,
            responseHandler, function (xhr) {
                responseHandler(xhr.responseText, xhr.statusText, xhr);
            });
        viewFenceByData(selectedAreaGeoJson, queryName, areaName, null, 'Exit');
    }
}

function setStationeryAlert(leafletId) {

    var selectedAreaGeoJson = map._layers[leafletId].toGeoJSON().geometry;

    //if a circle is drawn adding radius for the object
    if (selectedAreaGeoJson.type == "Point") {

        var radius = map._layers[leafletId]._mRadius;
        selectedAreaGeoJson["radius"] = radius;
    }

    var selectedProcessedAreaGeoJson = JSON.stringify(selectedAreaGeoJson).replace(/"/g, "'");

    var stationeryName = $("#stationaryAlertAreaName").val();
    var queryName = stationeryName;
    var fluctuationRadius = $("#fRadius").val();
    var time = $("#time").val();

    if (stationeryName == null || stationeryName === undefined || stationeryName == "") {
        var message = "Stationery Name cannot be empty.";
        noty({text: message, type: 'error'});
    } else if ($.trim(stationeryName).indexOf(" ") > -1) {
        var message = "Stationery Name cannot contain spaces.";
        noty({text: message, type: 'error'});
    } else if (fluctuationRadius == null || fluctuationRadius === undefined || fluctuationRadius == "") {
        var message = "Fluctuation Radius cannot be empty.";
        noty({text: message, type: 'error'});
    } else if (time == null || time === undefined || time == "") {
        var message = "Time cannot be empty.";
        noty({text: message, type: 'error'});
    } else {
        stationeryName = $.trim(stationeryName);
        var data = {
            'parseData': JSON.stringify({
                'geoFenceGeoJSON': selectedProcessedAreaGeoJson,
                'stationeryName': stationeryName,
                'stationeryTime': time,
                'fluctuationRadius': fluctuationRadius
            }),
            'stationeryTime': time,
            'fluctuationRadius': fluctuationRadius,
            'executionPlan': 'Stationery',
            'customName': stationeryName, // TODO: fix , When template copies there can be two queryName and areaName id elements in the DOM
            'queryName': queryName,
            'cepAction': 'deploy',
            'deviceId': deviceId
        };
        var serviceUrl = '/api/device-mgt/v1.0/geo-services/alerts/Stationery';
        var responseHandler = function (data, textStatus, xhr) {
            closeTools(leafletId);
            if (xhr.status == 200) {
                noty({text: 'Successfully added alert', type: 'success'});
            } else {
                var ptrn = /(?:<am\:description>)(.*)(?:<\/am\:description>)/g;
                var errorTxt;
                var result = (ptrn.exec(data));
                if (result) {
                    errorTxt = result.length > 1 ? result[1] : data;
                } else {
                    errorTxt = data;
                }
                noty({text: textStatus + ' : ' + errorTxt, type: 'error'});
            }
        };
        invokerUtil.post(serviceUrl,
            data,
            responseHandler, function (xhr) {
                responseHandler(xhr.responseText, xhr.statusText, xhr);
            });
        viewFenceByData(selectedProcessedAreaGeoJson, queryName, stationeryName, time, 'Stationery');
    }


}

var toggeled = false;

function setTrafficAlert(leafletId) {
    /*
     * TODO: replace double quote to single quote because of a conflict when deploying execution plan in CEP
     * this is against JSON standards so has been re-replaced when getting the data from governance registry
     * (look in get_alerts for .replace() method)
     * */
    var selectedAreaGeoJson = map._layers[leafletId].toGeoJSON().geometry;

    //if a circle is drawn adding radius for the object
    if (selectedAreaGeoJson.type == "Point") {

        var radius = map._layers[leafletId]._mRadius;
        selectedAreaGeoJson["radius"] = radius;
    }

    var selectedProcessedAreaGeoJson = JSON.stringify(selectedAreaGeoJson).replace(/"/g, "'");

    var areaName = $("#trafficAlertAreaName").val();
    var queryName = areaName;
    //var time = $("#time").val();

    if (areaName == null || areaName === undefined || areaName == "") {
        var message = "Area Name cannot be empty.";
        noty({text: message, type: 'error'});
    } else if ($.trim(areaName).indexOf(" ") > -1) {
        var message = "Area Name cannot contain spaces.";
        noty({text: message, type: 'error'});
    } else {
        areaName = $.trim(areaName);
        var data = {
            'parseData': JSON.stringify({
                'geoFenceGeoJSON': selectedProcessedAreaGeoJson,
                'areaName': areaName
            }),
            'executionPlan': 'Traffic',
            'customName': areaName, // TODO: fix , When template copies there can be two queryName and areaName id elements in the DOM
            'queryName': queryName,
            'cepAction': 'deploy',
            'deviceId': deviceId
        };

        var serviceUrl = '/api/device-mgt/v1.0/geo-services/alerts/Traffic';
        var responseHandler = function (data, textStatus, xhr) {
            closeTools(leafletId);
            if (xhr.status == 200) {
                noty({text: 'Successfully added alert', type: 'success'});
            } else {
                var ptrn = /(?:<am\:description>)(.*)(?:<\/am\:description>)/g;
                var errorTxt;
                var result = (ptrn.exec(data));
                if (result) {
                    errorTxt = result.length > 1 ? result[1] : data;
                } else {
                    errorTxt = data;
                }
                noty({text: textStatus + ' : ' + errorTxt, type: 'error'});
            }
        };
        invokerUtil.post(serviceUrl,
            data,
            responseHandler, function (xhr) {
                responseHandler(xhr.responseText, xhr.statusText, xhr);
            });
    }
}

function removeGeoFence(geoFenceElement, id) {
    var queryName = $(geoFenceElement).attr('data-queryName');
    var areaName = $(geoFenceElement).attr('data-areaName');

    var serviceUrl = '/api/device-mgt/v1.0/geo-services/alerts/' + id + '?queryName='
        + queryName;
    invokerUtil.delete(serviceUrl, function (response) {
            noty({
                text: 'Successfully removed ' + id + ' alert',
                type: 'success'
            });
            closeAll();
        },
        function (xhr) {
            noty({
                text: 'Could not remove ' + id + ' alert',
                type: 'error'
            })
        });
}


function getAlertsHistory(timeFrom, timeTo) {
    var timeRange = '';
    if (timeFrom && timeTo) {
        timeRange = '?from=' + timeFrom + '&to=' + timeTo;
    }
    var serviceUrl = '/api/device-mgt/v1.0/geo-services/alerts/history' + timeRange;
    invokerUtil.get(serviceUrl,
        function (data) {
            geoAlertsBar.clearAllAlerts();
            var alerts = JSON.parse(data);
            $.each(alerts, function (key, val) {
                if (val.values) {
                    val = val.values;
                }
                var msg = val.information.replace("Alerts: ,", "").charAt(0).toUpperCase() +
                    val.information.replace("Alerts: ,", "").slice(1) + " - " + timeSince(val.timeStamp);
                switch (val.state) {
                    case "NORMAL":
                        return;
                    case "WARNING":
                        geoAlertsBar.addAlert('warn', msg, val);
                        break;
                    case "ALERTED":
                        geoAlertsBar.addAlert('danger', msg, val);
                        break;
                    case "OFFLINE":
                        geoAlertsBar.addAlert('info', msg, val);
                        break;
                }
            });
        }, function (message) {
        });
}


function setProximityAlert() {

    var proximityDistance = $("#proximityDistance").val();
    var proximityTime = $("#proximityTime").val();

    if (proximityDistance == null || proximityDistance === undefined || proximityDistance == "") {
        var message = "Proximity distance cannot be empty.";
        noty({text: message, type: 'error'});
    } else if (proximityTime == null || proximityTime === undefined || proximityTime == "") {
        var message = "Proximity Time cannot be empty.";
        noty({text: message, type: 'error'});
    } else {
        var data = {
            'parseData': JSON.stringify({
                'proximityTime': proximityTime,
                'proximityDistance': proximityDistance
            }),
            'proximityTime': proximityTime,
            'proximityDistance': proximityDistance,
            'executionPlan': 'Proximity',
            'customName': null,
            'cepAction': 'edit',
            'deviceId': deviceId
        };
        var serviceUrl = '/api/device-mgt/v1.0/geo-services/alerts/Proximity';
        var responseHandler = function (data, textStatus, xhr) {
            closeAll();
            if (xhr.status == 200) {
                noty({text: 'Successfully added alert', type: 'success'});
            } else {
                var ptrn = /(?:<am\:description>)(.*)(?:<\/am\:description>)/g;
                var errorTxt;
                var result = (ptrn.exec(data));
                if (result) {
                    errorTxt = result.length > 1 ? result[1] : data;
                } else {
                    errorTxt = data;
                }
                noty({text: textStatus + ' : ' + errorTxt, type: 'error'});
            }
        };
        invokerUtil.put(serviceUrl,
            data,
            responseHandler, function (xhr) {
                responseHandler(xhr.responseText, xhr.statusText, xhr);
            });

    }
}

// TODO:this is not a remote call , move this to application.js
function closeAll() {
    $('.modal').modal('hide');
    setTimeout(function () {
        $.noty.closeAll();
    }, 100);
}

