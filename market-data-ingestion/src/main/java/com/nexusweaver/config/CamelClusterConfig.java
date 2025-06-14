package com.nexusweaver.config;

import org.apache.camel.cluster.CamelClusterService;
import org.apache.camel.component.kubernetes.cluster.KubernetesClusterService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Camel Clustering Configuration
 * 
 * Uses Kubernetes leader election to ensure only one pod
 * handles singleton routes (timer, WebSocket, TCP connections).
 * 
 * Architecture:
 * - LEADER POD: Handles all clustered routes (timer, TCP, WebSocket)
 * - FOLLOWER PODS: Handle distributed processing routes only
 */
@Configuration
public class CamelClusterConfig {

    @Value("${spring.application.name:market-data-ingestion}")
    private String applicationName;
    
    @Value("${camel.cluster.namespace:nexus-weaver}")
    private String namespace;

    /**
     * Kubernetes-based cluster service for leader election
     */
    @Bean
    public CamelClusterService kubernetesClusterService() {
        KubernetesClusterService cluster = new KubernetesClusterService();
        
        // Cluster configuration
        cluster.setId("nexus-camel-cluster");
        cluster.setOrder(1);
        
        // Configuration via properties - Camel 4.x uses configuration properties
        System.setProperty("camel.component.kubernetes.cluster.pod-name", System.getenv("HOSTNAME"));
        System.setProperty("camel.component.kubernetes.cluster.namespace", namespace);
        System.setProperty("camel.component.kubernetes.cluster.resource-name-prefix", applicationName + "-leader");
        System.setProperty("camel.component.kubernetes.cluster.lease-duration-seconds", "15");
        System.setProperty("camel.component.kubernetes.cluster.renew-deadline-seconds", "10");
        System.setProperty("camel.component.kubernetes.cluster.retry-period-seconds", "2");
        
        return cluster;
    }
} 