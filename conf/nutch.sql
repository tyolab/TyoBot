create database nutch;

CREATE TABLE `webpage` (
 
`id` varchar(767) CHARACTER SET latin1 NOT NULL,
 
`headers` blob,
 
`text` mediumtext DEFAULT NULL,
 
`status` int(11) DEFAULT NULL,
 
`markers` blob,
 
`parseStatus` blob,
 
`modifiedTime` bigint(20) DEFAULT NULL,
 
`score` float DEFAULT NULL,
 
`typ` varchar(32) CHARACTER SET latin1 DEFAULT NULL,
 
`baseUrl` varchar(512) CHARACTER SET latin1 DEFAULT NULL,
 
`content` mediumblob,
 
`title` varchar(2048) DEFAULT NULL,
 
`reprUrl` varchar(512) CHARACTER SET latin1 DEFAULT NULL,
 
`fetchInterval` int(11) DEFAULT NULL,
 
`prevFetchTime` bigint(20) DEFAULT NULL,
 
`inlinks` mediumblob,
 
`prevSignature` blob,
 
`outlinks` mediumblob,
 
`fetchTime` bigint(20) DEFAULT NULL,
 
`retriesSinceFetch` int(11) DEFAULT NULL,
 
`protocolStatus` blob,
 
`signature` blob,
 
`metadata` blob,
 
PRIMARY KEY (`id`)
 
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
