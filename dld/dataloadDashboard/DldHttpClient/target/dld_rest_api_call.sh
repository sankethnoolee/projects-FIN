#BASH Command

echo 'Entered'

taskName='RECP LOANS TO FCT AGG LOAN'
echo $taskName

taskStartTime='07-09-2017 09:02:44.000000 PM'
echo $taskStartTime

taskEndTime='07-09-2017 10:02:44.000000 PM'
echo $taskEndTime

appliedRows=6
echo $appliedRows

affectedRows=6
echo $affectedRows

rejectedRows=0
echo $rejectedRows

targetArea='Subject_Area'
echo $targetArea

flowSeqNo=0
echo $flowSeqNo

flowType='Daily FII'
echo $flowType

businessPeriodId='31-12-2014'
echo $businessPeriodId

runPeriodId='31-12-2014'
echo $runPeriodId

srcCnt=6
echo $srcCnt

tgtCnt=6
echo $tgtCnt

runStatus='COMPLETED'
echo $runStatus

clientCode='PLT'
echo $clientCode

taskTechName='Task_1'
echo $taskTechName

taskTechSubName='Task_1'
echo $taskTechSubName

taskRepo='DATAHUB'
echo $taskRepo

echo 'exited...!!!'

java -jar dldHttpClient-0.0.1-SNAPSHOT-jar-with-dependencies.jar endPointURL#http://localhost:8090/dldwebapplication,clientCode#PLT,taskRepo#DATAHUB,taskName#RECP LOANS TO FCT AGG LOAN,flowType#Daily FII,flowSeqNo#0,runStatus#COMPLETED,taskTechName#Task_1,taskTechSubName#Task_1,runDetails#,srcCnt#6,tgtCnt#6,rejectedRows#0,affectedRows#0,appliedRows#0,taskStartTime#,taskEndTime#,runPeriodId#20141231,businessPeriodId#20141231

