<?php

$http_origin = $_SERVER["HTTP_ORIGIN"];

if (strpos($http_origin, 'youtube') !== false)
    header('Access-Control-Allow-Origin: '.$http_origin);

$channel = @$_POST["user_channel"];
$video = @$_POST["video"];
$type = @$_POST["type"];
$username = @$_POST["username"];
$subbed_channel = @$_POST["subbed_channel"];
$main_channel = @$_POST["main_channel"];

$url = "http://5.135.180.12/monkey/sykoreward/index.php?user_channel=".$channel."&video=".$video."&type=".$type."&username=".$username;

if(isset($subbed_channel))
	$url .= "&subbed_channel=".$subbed_channel;

if(isset($main_channel))
	$url .= "&main_channel=".$main_channel;

echo file_get_contents($url);