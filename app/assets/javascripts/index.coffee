$ ->
  ws = new WebSocket $("body").data("ws-url")
  ws.onmessage = (event) ->
    getAllTweets(message)


getAllTweets = (message) ->
    tweets = []
    $log.debug "getAllTweets()"
    $log.debug "listTweets()"
    deferred = @$q.defer()

    $http.get("/tweets")
    .success((data, status, headers) =>
            $log.debug "Promise returned #{data.length} Tweets"
            tweets = data.reverse())
            
            
            # EXAMPLE OF GENERATING HTML CODE?
            #chart = $("<div>").addClass("chart").prop("id", message.symbol)
            #chartHolder = $("<div>").addClass("chart-holder").append(chart)
            #chartHolder.append($("<p>").text("henry"))
            #detailsHolder = $("<div>").addClass("details-holder")
            #flipper = $("<div>").addClass("flipper").append(chartHolder).append(detailsHolder).attr("data-content", message.symbol)
            #flipContainer = $("<div>").addClass("flip-container").append(flipper).click (event) -> 
            #  handleFlip($(this))
            #$("#stocks").prepend(flipContainer)
            #plot = chart.plot([getChartArray(message.history)], getChartOptions(message.history)).data("plot"))
            
    .error((data, status, headers) =>
            $log.error("Failed to list Tweets - status #{status}")
            $log.error "Unable to get Tweets: #{error}"
            deferred.reject(data))
    deferred.promise