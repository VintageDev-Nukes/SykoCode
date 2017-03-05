<?php

$http_origin = $_SERVER["HTTP_ORIGIN"];

if (strpos($http_origin, 'youtube') !== false)
    header('Access-Control-Allow-Origin: '.$http_origin);

$channel = $_POST["user_channel"];
$host_channel = $_POST["host_channel"];
$username = $_POST["username"];

if(isset($username))
	echo file_get_contents("http://5.135.180.12/monkey/sykoreward/checker.php?channel=".$channel."&host_channel=".$host_channel."&username=".$username);
else
	echo file_get_contents("http://5.135.180.12/monkey/sykoreward/checker.php?channel=".$channel."&host_channel=".$host_channel);