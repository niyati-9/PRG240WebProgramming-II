param(
    [string]$OutputFile = "system-metrics-$(Get-Date -Format 'yyyyMMdd-HHmmss').log"
)

Write-Host "=====================================================================" -ForegroundColor Cyan
Write-Host "System Resource Monitor for K6 Load Testing - Khel App" -ForegroundColor Cyan
Write-Host "=====================================================================" -ForegroundColor Cyan
Write-Host "Output file: $OutputFile" -ForegroundColor Green
Write-Host "Monitoring interval: 5 seconds" -ForegroundColor Yellow
Write-Host "Press Ctrl+C to stop monitoring" -ForegroundColor Red
Write-Host "=====================================================================" -ForegroundColor Cyan
Write-Host ""

# Initialize output file
$header = "System Resource Monitoring - K6 Load Test (Khel App)`nStarted: $(Get-Date)`nHostname: $env:COMPUTERNAME`n====================================================================`n`nTimestamp,CPU%,Memory%,MemoryMB,JavaCPU,JavaMemMB"
$header | Out-File -FilePath $OutputFile -Encoding UTF8

Write-Host "Monitoring started..." -ForegroundColor Green
Write-Host ""
Write-Host "Timestamp            CPU%    MemoryMB   JavaCPU   JavaMemMB" -ForegroundColor Yellow
Write-Host "----------------------------------------------------------------"

$count = 0

try {
    while ($true) {
        $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

        # Get Java process
        $javaProcess = Get-Process -Name "java" -ErrorAction SilentlyContinue | Select-Object -First 1

        if ($null -eq $javaProcess) {
            $javaCPU = 0
            $javaMem = 0
        } else {
            $javaCPU = [math]::Round($javaProcess.CPU, 2)
            $javaMem = [math]::Round($javaProcess.WorkingSet64 / 1MB, 2)
        }

        # CPU usage
        try {
            $cpuCounter = Get-Counter '\Processor(_Total)\% Processor Time' -ErrorAction SilentlyContinue
            $cpuUsage = [math]::Round($cpuCounter.CounterSamples.CookedValue, 2)
        } catch {
            $cpuUsage = 0
        }

        # Memory usage
        $computerMemory = Get-CimInstance -ClassName Win32_OperatingSystem
        $totalMemory = $computerMemory.TotalVisibleMemorySize
        $freeMemory = $computerMemory.FreePhysicalMemory
        $usedMemory = $totalMemory - $freeMemory
        $memoryPercent = [math]::Round(($usedMemory / $totalMemory) * 100, 2)
        $memoryMB = [math]::Round($usedMemory / 1024, 2)

        # Log to file
        $logLine = "$timestamp,$cpuUsage,$memoryPercent,$memoryMB,$javaCPU,$javaMem"
        $logLine | Out-File -FilePath $OutputFile -Append -Encoding UTF8

        # Display to console
        $displayLine = "{0,-20} {1,6}% {2,10}MB {3,9} {4,11}MB" -f $timestamp, $cpuUsage, $memoryMB, $javaCPU, $javaMem
        Write-Host $displayLine -ForegroundColor Green

        $count++

        if ($count % 12 -eq 0) {
            Write-Host "----------------------------------------------------------------"
            Write-Host "Monitoring for $($count * 5) seconds... ($([math]::Floor($count / 12)) minute(s))" -ForegroundColor Cyan
            Write-Host "----------------------------------------------------------------"
        }

        Start-Sleep -Seconds 5
    }
}
catch {
    Write-Host "`nMonitoring stopped." -ForegroundColor Yellow
    Write-Host "Log saved to: $OutputFile" -ForegroundColor Green
}
