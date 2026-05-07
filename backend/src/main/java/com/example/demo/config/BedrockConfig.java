package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

@Configuration
public class BedrockConfig {

    // application.yml의 aws.access-key / aws.secret-key / aws.region 주입
    @Value("${aws.access-key}")
    private String accessKey;

    @Value("${aws.secret-key}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    /**
     * BedrockRuntimeClient 빈 등록.
     * BedrockClient에서 @RequiredArgsConstructor로 주입받아 사용한다.
     * 로컬 개발: .env의 AWS_ACCESS_KEY / AWS_SECRET_KEY 사용
     * 배포 환경: EC2 IAM Role 사용 시 StaticCredentialsProvider 대신
     *            InstanceProfileCredentialsProvider로 교체 권장
     */
    @Bean
    public BedrockRuntimeClient bedrockRuntimeClient() {
        return BedrockRuntimeClient.builder()
                .region(Region.of(region))
                .credentialsProvider(
                    StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                    )
                )
                .build();
    }
}
