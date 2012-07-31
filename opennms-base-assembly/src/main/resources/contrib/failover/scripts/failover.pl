#!/usr/bin/perl

use IO::Handle;
my $log = '/opt/opennms/logs/failover.log';
open MYLOG, '>>', $log;
STDOUT->fdopen( \*MYLOG, 'w' );
STDERR->fdopen( \*MYLOG, 'w' );

        my $isVIP = `ifconfig | grep eth0:0`;
        my $vipTime = "/var/cache/jboss/opennms/vipTime.txt";
        my $snmpTargetReset = "/var/cache/jboss/opennms/snmpTargetReset";
        my $pendingDD = "/var/cache/jboss/opennms/pendingDD.txt";
        my $ear = "/usr/local/jboss/server/all/deploy/011/opennms.ear";
        if ($isVIP ne "")  {## current is VIP
                my $time = time();
                print "now is $time\n";
                if (-e $vipTime) {
                        # count total time lapsed
                        my $start = `cat $vipTime`;
                        chomp $start;
                        $delta = $time - $start;
                        print "VIP has been owned $delta\n";
                        if ($delta >  180 && ! -e $snmpTargetReset) {
				print "starting opennms services\n";
				system("sh /opt/opennms/contrib/failover/scripts/failover.sh >> $log 2>&1");
				my $running = isServiceRunning();
				if ($running == 0) {	
				  if (isDevIpConfigured()){
				# if there is no dev management IP,
				# VIP is the snmp target, so no need to resync 
                                	print "trigger snmp target reset\n";
                                	system("touch $snmpTargetReset");
				  }
				}else {
					print "ERROR: service opennms failed to run\n";
				}
                        }elsif ($delta > 0 && $delta < 180) {
                                if (-e $snmpTargetReset) {
                                        system("rm $snmpTargetReset");
                                }
                        }elsif ($delta > 180 ) {
                               print "previous manual stop, starting without discovery";
                               system("sh /opt/opennms/contrib/failover/scripts/failover.sh >> $log 2>&1");
                       }

                }else { # start timer
                        system("echo $time >$vipTime");
                        print "start timer with $time\n";
                }
        }else {# not vip, delete timer
                if (-e $vipTime)  {
                        system("rm  $vipTime");
                        system("rm $snmpTargetReset");
                        print "remove timer\n";
                }
        }

sub isServiceRunning {
my $count = 0;
my $done = 0;
while ($count < 10 && $done == 0) {
        my $return =  system("service opennms status");
        if ($return != 0) {
                my $status = system("service opennms status | grep running");
                if ($status == 0 ) {
                        print "partially running, give 15 seconds\n";
                        sleep 15;
                        $count++;
                }else {
                        print "ERROR: installation issue\n";
                        $count = 10;
                }
        }else {
                $done = 1;
        }
}
if ($done == 1) {
        print "\n## service runing, move on";
        return 0;
}else {
        print "\n##ERROR: waited 100 seconds, failed faileover";
        return 1;
}
}

sub isDevIpConfigured {
    my $str = `ifconfig eth3 | grep 'inet addr'`;
    if ($str eq '') {
	return 0;	
    }
    else {
	return 1;
    }
}
