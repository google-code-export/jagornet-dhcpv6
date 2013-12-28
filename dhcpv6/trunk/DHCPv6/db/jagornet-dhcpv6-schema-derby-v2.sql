CREATE TABLE DHCPLEASE (
	IPADDRESS VARCHAR(16) FOR BIT DATA PRIMARY KEY,
    DUID VARCHAR(130) FOR BIT DATA NOT NULL,
    IATYPE SMALLINT,
    IAID BIGINT,
    PREFIXLEN SMALLINT,
	STATE SMALLINT,
    STARTTIME TIMESTAMP,
    PREFERREDENDTIME TIMESTAMP,
    VALIDENDTIME TIMESTAMP,
    IA_OPTIONS BLOB,
    IPADDR_OPTIONS BLOB
);
CREATE INDEX TUPLE_NDX ON DHCPLEASE (DUID, IATYPE, IAID);
CREATE INDEX DUID_NDX ON DHCPLEASE (DUID);
CREATE INDEX IATYPE_NDX ON DHCPLEASE (IATYPE);
CREATE INDEX STATE_NDX ON DHCPLEASE (STATE);
CREATE INDEX ENDTIME_NDX ON DHCPLEASE (VALIDENDTIME);
