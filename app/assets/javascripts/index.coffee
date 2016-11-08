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
            tweets = data.reverse()
            
            
            
            chart = $("<div>").addClass("chart").prop("id", message.symbol)
            chartHolder = $("<div>").addClass("chart-holder").append(chart)
            chartHolder.append($("<p>").text("henry"))
            detailsHolder = $("<div>").addClass("details-holder")
            flipper = $("<div>").addClass("flipper").append(chartHolder).append(detailsHolder).attr("data-content", message.symbol)
            flipContainer = $("<div>").addClass("flip-container").append(flipper).click (event) -> 
              handleFlip($(this))
            $("#stocks").prepend(flipContainer)
            plot = chart.plot([getChartArray(message.history)], getChartOptions(message.history)).data("plot")
                        )
    .error((data, status, headers) =>
            $log.error("Failed to list Tweets - status #{status}")
            $log.error "Unable to get Tweets: #{error}"
            deferred.reject(data)
        )
    deferred.promise


getPricesFromArray = (data) ->
  (v[1] for v in data)

getChartArray = (data) ->
  ([i, v] for v, i in data)

getChartOptions = (data) ->
  series:
    shadowSize: 0
  yaxis:
    min: getAxisMin(data)
    max: getAxisMax(data)
  xaxis:
    show: false

getAxisMin = (data) ->
  Math.min.apply(Math, data) * 0.9

getAxisMax = (data) ->
  Math.max.apply(Math, data) * 1.1

populateStockHistory = (message) ->
    chart = $("<div>").addClass("chart").prop("id", message.symbol)
    chartHolder = $("<div>").addClass("chart-holder").append(chart)
    chartHolder.append($("<p>").text("henry"))
    detailsHolder = $("<div>").addClass("details-holder")
    flipper = $("<div>").addClass("flipper").append(chartHolder).append(detailsHolder).attr("data-content", message.symbol)
    flipContainer = $("<div>").addClass("flip-container").append(flipper).click (event) -> 
      handleFlip($(this))
    $("#stocks").prepend(flipContainer)
    plot = chart.plot([getChartArray(message.history)], getChartOptions(message.history)).data("plot")

updateStockChart = (message) ->
  if ($("#" + message.symbol).size() > 0)
    plot = $("#" + message.symbol).data("plot")
    data = getPricesFromArray(plot.getData()[0].data)
    data.shift()
    data.push(message.price)
    plot.setData([getChartArray(data)])
    # update the yaxes if either the min or max is now out of the acceptable range
    yaxes = plot.getOptions().yaxes[0]
    if ((getAxisMin(data) < yaxes.min) || (getAxisMax(data) > yaxes.max))
      # reseting yaxes
      yaxes.min = getAxisMin(data)
      yaxes.max = getAxisMax(data)
      plot.setupGrid()
    # redraw the chart
    plot.draw()

handleFlip = (container) ->
  if (container.hasClass("flipped"))
    container.removeClass("flipped")
    container.find(".details-holder").empty()
