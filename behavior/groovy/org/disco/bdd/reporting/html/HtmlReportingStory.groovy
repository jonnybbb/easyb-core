import org.disco.easyb.domain.BehaviorFactory
import org.disco.easyb.listener.ResultsCollector
import org.disco.easyb.listener.BroadcastListener
import org.disco.easyb.report.HtmlReportWriter

before_each "initialize the collectors", {
  and
  given "the collectors are initialized", {
    broadcastListener = new BroadcastListener();
    resultsCollector = new ResultsCollector();
    broadcastListener.registerListener(resultsCollector);
  }
}

findContentDiv = {
  xmlReport.body.div.find { it['@id'] == 'page' }.div.find { it['@id'] == 'content'}
}

findSummariesDiv = {
  findContentDiv().div.find { it['@class'] == 'post' }.div.find { it['@class'] == 'entry' }.div.find { it['@id'] == 'Summaries' }
}

findStoriesListPlainDiv = {
  findContentDiv().div.find { it['@class'] == 'post' }.div.find { it['@class'] == 'entry' }.div.find { it['@id'] == 'StoriesListPlain' }
}

findStoriesListDiv = {
  findContentDiv().div.find { it['@class'] == 'post' }.div.find { it['@class'] == 'entry' }.div.find { it['@id'] == 'StoriesList' }
}

scenario "a passing, failing and pending scenario", {
  given "a story file with a passing, failing and pending scenario is loaded", {
    storyBehavior = BehaviorFactory.createBehavior(new File('./behavior/groovy/org/disco/bdd/reporting/html/PassingPendingFailing.story'))
  }

  when "the story is executed", {
    storyBehavior.execute(broadcastListener)
  }

  and
  when "the html reports are written", {
    htmlReportLocation = "./target/PassingPendingFailing-report.html"
    new HtmlReportWriter(htmlReportLocation).writeReport(resultsCollector)
  }

  then "the resulting html should have 3 total behaviors in the behavior summary", {
    xmlReport = new XmlSlurper().parse(new File((String)htmlReportLocation))
    contentDiv = findContentDiv()
    summariesDiv = findSummariesDiv()

    behaviorsSummaryRow = summariesDiv.table[0].tbody.tr

    behaviorsSummaryRow.td[0].text().shouldBe '3'
  }

  and "1 failing scenario", {
    behaviorsSummaryRow.td[1].text().shouldBe '1'
    behaviorsSummaryRow.td[1].@class.shouldBe 'stepResultFailed'
  }

  and "1 pending scenario" , {
    behaviorsSummaryRow.td[2].text().shouldBe '1'
    behaviorsSummaryRow.td[2].@class.shouldBe 'stepResultPending'
  }

  and
  then "should have a stories summary with one story", {
    storiesSummaryRow = summariesDiv.table[1].tbody.tr
    storiesSummaryRow.td[0].text().shouldBe '1'

  }

  and "3 scenarios", {
    storiesSummaryRow.td[1].text().shouldBe '3'
  }

  and "1 failed scenario", {
    storiesSummaryRow.td[2].text().shouldBe '1'
    storiesSummaryRow.td[2].@class.shouldBe 'stepResultFailed'
  }

  and "1 pending scenario", {
    storiesSummaryRow.td[3].text().shouldBe '1'
    storiesSummaryRow.td[3].@class.shouldBe 'stepResultPending'
  }

  and
  then "should have a specifications summary with no results", {
    specificationsSummaryRow = summariesDiv.table[2].tbody.tr
    specificationsSummaryRow.td[0].text().shouldBe '0'
    specificationsSummaryRow.td[1].text().shouldBe '0'
    specificationsSummaryRow.td[1].@class.shouldBe ''
    specificationsSummaryRow.td[2].text().shouldBe '0'
    specificationsSummaryRow.td[2].@class.shouldBe ''
  }

  and
  then "should have a stories list with a story name passing pending failing", {
    storiesListDiv = findStoriesListDiv()
    storyRow = storiesListDiv.table.tbody.tr[0]
    storyRow.td[0].a.text().shouldBe 'passing pending failing'
  }

  and "3 scenarios", {
    storyRow.td[1].text().shouldBe '3'
  }

  and "1 failed", {
    storyRow.td[2].text().shouldBe '1'
    storyRow.td[2].@class.shouldBe 'stepResultFailed'
  }

  and "1 pending", {
    storyRow.td[3].text().shouldBe '1'
    storyRow.td[3].@class.shouldBe 'stepResultPending'
  }

  and
  then "should have a scenario named pending scenario", {
    scenariosRow = storiesListDiv.table.tbody.tr[1]
    pendingScenarioRow = scenariosRow.td.table.tbody.tr[0]
    pendingScenarioRow.td[0].text().shouldBe 'pending scenario'
    pendingScenarioRow.td[0].@title.shouldBe 'Scenario'
    pendingScenarioRow.td[1].text().shouldBe 'pending'
    pendingScenarioRow.td[1].@title.shouldBe 'Result'
    pendingScenarioRow.td[1].@class.shouldBe 'stepResultPending'
  }

  and
  then "should have a scenario named failing scenario", {
    failingScenarioRow = scenariosRow.td.table.tbody.tr[1]
    failingScenarioRow.td[0].text().shouldBe 'failing scenario'
    failingScenarioRow.td[0].@title.shouldBe 'Scenario'
    failingScenarioRow.td[1].text().shouldBe 'failure'
    failingScenarioRow.td[1].@title.shouldBe 'Result'
    failingScenarioRow.td[1].@class.shouldBe 'stepResultFailed'

    failingScenarioComponentRow = scenariosRow.td.table.tbody.tr[2]
    failingScenarioComponentRow.td[0].text().shouldBe "then 1 does not equal 0 should fail"
    failingScenarioComponentRow.td[1].text().shouldBe "failure"
    failingScenarioComponentRow.td[1].@class.shouldBe 'stepResultFailed'

    failingScenarioComponentDetailsRow = scenariosRow.td.table.tbody.tr[3]
    failingScenarioComponentDetailsRow.td.strong.text().shouldBe "expected 0 but was 1"

  }

  and
  then "should have a scenario named passing scenario", {
    passingScenarioRow = scenariosRow.td.table.tbody.tr[4]
    passingScenarioRow.td[0].text().shouldBe 'passing scenario'
    passingScenarioRow.td[0].@title.shouldBe 'Scenario'
    passingScenarioRow.td[1].text().shouldBe 'success'
    passingScenarioRow.td[1].@title.shouldBe 'Result'
    passingScenarioRow.td[1].@class.shouldBe 'stepResultSuccess'

    passingScenarioComponentRow = scenariosRow.td.table.tbody.tr[5]
    passingScenarioComponentRow.@class.shouldBe "scenarioComponents"
    passingScenarioComponentRow.td[0].text().shouldBe "then 1 should equal 1"
    passingScenarioComponentRow.td[1].text().shouldBe "success"
    passingScenarioComponentRow.td[1].@class.shouldBe "stepResultSuccess"
  }

  and
  then "should have a plain story with name of passing pending failing", {
    storiesListPlainDiv = findStoriesListPlainDiv()

    storiesListPlainDiv.div[0].text().shouldBe "3 scenarios (including 1 pending), but status is failure! Total failures: 1"
    storiesListPlainDiv.div[1].text().contains "&nbsp;&nbsp;Story: passing pending failing "
  }

  and "passing pending failing scenarios", {
    storiesListPlainDiv.div[1].text().contains "&nbsp;&nbsp;&nbsp;&nbsp;scenario pending scenario"
    storiesListPlainDiv.div[1].text().contains "&nbsp;&nbsp;&nbsp;&nbsp;scenario failing scenario"
    storiesListPlainDiv.div[1].text().contains "&nbsp;&nbsp;&nbsp;&nbsp;scenario passing scenario"
    storiesListPlainDiv.div[1].text().contains "[FAILURE:"
  }

}
