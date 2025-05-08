//package silverpotion.userserver.batch;
//
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.JobParameter;
//import org.springframework.batch.core.JobParameters;
//import org.springframework.batch.core.JobParametersBuilder;
//import org.springframework.batch.core.launch.JobLauncher;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//@Component
//public class JobScheduler {
//    private final JobLauncher jobLauncher;
//    private final Job dailyMakingReport;
//    private final Job weeklyAverageHealthJob;
////    private final Job weeklyHealthReportJob;
//    private final Job monthlyAverageHealthJob;
//
//    public JobScheduler(JobLauncher jobLauncher,@Qualifier("dailyMakingReport") Job dailyMakingReport,
//                        @Qualifier("weeklyAverageHealthJob") Job weeklyAverageHealthJob,@Qualifier("monthlyAverageHealthJob") Job monthlyAverageHealthJob) {
//        this.jobLauncher = jobLauncher;
//        this.dailyMakingReport = dailyMakingReport;
//        this.weeklyAverageHealthJob = weeklyAverageHealthJob;
////        this.weeklyHealthReportJob = weeklyHealthReportJob;
//        this.monthlyAverageHealthJob = monthlyAverageHealthJob;
//    }
//    // 0. 일간 헬스리포트 생성
//    @Scheduled(fixedRate = 6000000) //10분마다 실행(테스트용)
////    @Scheduled(cron = "0 0 1 * * *") // 매일 새벽 1시마다 실행
//    public void runDailyHealthReportJob(){
//
//        try{
//            JobParameters jobParameters = new JobParametersBuilder()
//                    .addLong("timestamp",System.currentTimeMillis()) // 스프링 배치는 jobParameters가 동열하면 같은 job으로 재실행 안함 따라서 매번 실행시 timpstamp같은 고유한 값을 넣어줘야함, 매주 잡이 실행될 수 있도록 하는거
//                    .toJobParameters();
//
//            jobLauncher.run(dailyMakingReport,jobParameters); //실제로 배치 잡을 실행하는 부분
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//
//    }
//
////// 1. 주간 평균 헬스데이터,헬스리포트 생성
//    @Scheduled(fixedRate = 6000000) //10분마다 실행(테스트용)
////    @Scheduled(cron = "0 0 0 * * MON") // 매주 월요일 마다 실행
//    public void runWeeklyHealthDataJob(){
//
//        try{
//            JobParameters jobParameters = new JobParametersBuilder()
//                    .addLong("timestamp",System.currentTimeMillis()) // 스프링 배치는 jobParameters가 동열하면 같은 job으로 재실행 안함 따라서 매번 실행시 timpstamp같은 고유한 값을 넣어줘야함, 매주 잡이 실행될 수 있도록 하는거
//                    .toJobParameters();
//
//            jobLauncher.run(weeklyAverageHealthJob,jobParameters); //실제로 배치 잡을 실행하는 부분
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//
//    }
//
////   3. 월간 헬스데이터,헬스리포트 생성
//    @Scheduled(fixedRate = 6000000) //10분마다 실행(테스트용)
//    //    @Scheduled(cron = "0 0 0 1 * *") // 매달 1일 마다 실행
//    public void runMonthlyHealthDataJob(){
//
//        try{
//            JobParameters jobParameters = new JobParametersBuilder()
//                    .addLong("timestamp",System.currentTimeMillis()) // 스프링 배치는 jobParameters가 동열하면 같은 job으로 재실행 안함 따라서 매번 실행시 timpstamp같은 고유한 값을 넣어줘야함, 매주 잡이 실행될 수 있도록 하는거
//                    .toJobParameters();
//
//            jobLauncher.run(monthlyAverageHealthJob,jobParameters); //실제로 배치 잡을 실행하는 부분
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//
//    }
//
//
//}
