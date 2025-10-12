var stats = {
    type: "GROUP",
name: "All Requests",
path: "",
pathFormatted: "group_missing-name--1146707516",
stats: {
    "name": "All Requests",
    "numberOfRequests": {
        "total": "40",
        "ok": "0",
        "ko": "40"
    },
    "minResponseTime": {
        "total": "5",
        "ok": "-",
        "ko": "5"
    },
    "maxResponseTime": {
        "total": "74",
        "ok": "-",
        "ko": "74"
    },
    "meanResponseTime": {
        "total": "17",
        "ok": "-",
        "ko": "17"
    },
    "standardDeviation": {
        "total": "19",
        "ok": "-",
        "ko": "19"
    },
    "percentiles1": {
        "total": "11",
        "ok": "-",
        "ko": "11"
    },
    "percentiles2": {
        "total": "13",
        "ok": "-",
        "ko": "13"
    },
    "percentiles3": {
        "total": "63",
        "ok": "-",
        "ko": "63"
    },
    "percentiles4": {
        "total": "74",
        "ok": "-",
        "ko": "74"
    },
    "group1": {
    "name": "t < 800 ms",
    "htmlName": "t < 800 ms",
    "count": 0,
    "percentage": 0.0
},
    "group2": {
    "name": "800 ms <= t < 1200 ms",
    "htmlName": "t >= 800 ms <br> t < 1200 ms",
    "count": 0,
    "percentage": 0.0
},
    "group3": {
    "name": "t >= 1200 ms",
    "htmlName": "t >= 1200 ms",
    "count": 0,
    "percentage": 0.0
},
    "group4": {
    "name": "failed",
    "htmlName": "failed",
    "count": 40,
    "percentage": 100.0
},
    "meanNumberOfRequestsPerSecond": {
        "total": "2.22",
        "ok": "-",
        "ko": "2.22"
    }
},
contents: {
"req_register--625569085": {
        type: "REQUEST",
        name: "Register",
path: "Register",
pathFormatted: "req_register--625569085",
stats: {
    "name": "Register",
    "numberOfRequests": {
        "total": "40",
        "ok": "0",
        "ko": "40"
    },
    "minResponseTime": {
        "total": "5",
        "ok": "-",
        "ko": "5"
    },
    "maxResponseTime": {
        "total": "74",
        "ok": "-",
        "ko": "74"
    },
    "meanResponseTime": {
        "total": "17",
        "ok": "-",
        "ko": "17"
    },
    "standardDeviation": {
        "total": "19",
        "ok": "-",
        "ko": "19"
    },
    "percentiles1": {
        "total": "11",
        "ok": "-",
        "ko": "11"
    },
    "percentiles2": {
        "total": "13",
        "ok": "-",
        "ko": "13"
    },
    "percentiles3": {
        "total": "63",
        "ok": "-",
        "ko": "63"
    },
    "percentiles4": {
        "total": "74",
        "ok": "-",
        "ko": "74"
    },
    "group1": {
    "name": "t < 800 ms",
    "htmlName": "t < 800 ms",
    "count": 0,
    "percentage": 0.0
},
    "group2": {
    "name": "800 ms <= t < 1200 ms",
    "htmlName": "t >= 800 ms <br> t < 1200 ms",
    "count": 0,
    "percentage": 0.0
},
    "group3": {
    "name": "t >= 1200 ms",
    "htmlName": "t >= 1200 ms",
    "count": 0,
    "percentage": 0.0
},
    "group4": {
    "name": "failed",
    "htmlName": "failed",
    "count": 40,
    "percentage": 100.0
},
    "meanNumberOfRequestsPerSecond": {
        "total": "2.22",
        "ok": "-",
        "ko": "2.22"
    }
}
    }
}

}

function fillStats(stat){
    $("#numberOfRequests").append(stat.numberOfRequests.total);
    $("#numberOfRequestsOK").append(stat.numberOfRequests.ok);
    $("#numberOfRequestsKO").append(stat.numberOfRequests.ko);

    $("#minResponseTime").append(stat.minResponseTime.total);
    $("#minResponseTimeOK").append(stat.minResponseTime.ok);
    $("#minResponseTimeKO").append(stat.minResponseTime.ko);

    $("#maxResponseTime").append(stat.maxResponseTime.total);
    $("#maxResponseTimeOK").append(stat.maxResponseTime.ok);
    $("#maxResponseTimeKO").append(stat.maxResponseTime.ko);

    $("#meanResponseTime").append(stat.meanResponseTime.total);
    $("#meanResponseTimeOK").append(stat.meanResponseTime.ok);
    $("#meanResponseTimeKO").append(stat.meanResponseTime.ko);

    $("#standardDeviation").append(stat.standardDeviation.total);
    $("#standardDeviationOK").append(stat.standardDeviation.ok);
    $("#standardDeviationKO").append(stat.standardDeviation.ko);

    $("#percentiles1").append(stat.percentiles1.total);
    $("#percentiles1OK").append(stat.percentiles1.ok);
    $("#percentiles1KO").append(stat.percentiles1.ko);

    $("#percentiles2").append(stat.percentiles2.total);
    $("#percentiles2OK").append(stat.percentiles2.ok);
    $("#percentiles2KO").append(stat.percentiles2.ko);

    $("#percentiles3").append(stat.percentiles3.total);
    $("#percentiles3OK").append(stat.percentiles3.ok);
    $("#percentiles3KO").append(stat.percentiles3.ko);

    $("#percentiles4").append(stat.percentiles4.total);
    $("#percentiles4OK").append(stat.percentiles4.ok);
    $("#percentiles4KO").append(stat.percentiles4.ko);

    $("#meanNumberOfRequestsPerSecond").append(stat.meanNumberOfRequestsPerSecond.total);
    $("#meanNumberOfRequestsPerSecondOK").append(stat.meanNumberOfRequestsPerSecond.ok);
    $("#meanNumberOfRequestsPerSecondKO").append(stat.meanNumberOfRequestsPerSecond.ko);
}
