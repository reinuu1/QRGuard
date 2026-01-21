package com.example.qrsafe.ui.qrsafe.data

import androidx.compose.ui.graphics.Color

object MitreRepository {
    fun getRealMitreData(): List<MitreTechnique> {
        return listOf(
            MitreTechnique(
                id = "T1566",
                name = "Phishing",
                tactic = "Initial Access",
                description = "Adversaries may send phishing messages to gain access to victim systems. All forms of phishing are electronically delivered social engineering.",
                mitigation = "M1021: User Training. Train users to be cautious with email attachments and links.",
                color = Color(0xFFFF5252) // Red for Initial Access
            ),
            MitreTechnique(
                id = "T1059",
                name = "Command and Scripting Interpreter",
                tactic = "Execution",
                description = "Adversaries may abuse command and script interpreters to execute commands, scripts, or binaries.",
                mitigation = "M1038: Execution Prevention. Use application control to prevent execution of unapproved scripts.",
                color = Color(0xFFFFAB00) // Amber for Execution
            ),
            MitreTechnique(
                id = "T1003",
                name = "OS Credential Dumping",
                tactic = "Credential Access",
                description = "Adversaries may attempt to dump credentials to obtain account login and password information (e.g., LSASS memory).",
                mitigation = "M1043: Credential Access Protection. Ensure strict local administrator password management.",
                color = Color(0xFFE040FB) // Purple for Credential Access
            ),
            MitreTechnique(
                id = "T1078",
                name = "Valid Accounts",
                tactic = "Defense Evasion",
                description = "Adversaries may obtain and abuse credentials of existing accounts as a means of gaining Initial Access, Persistence, Privilege Escalation, or Defense Evasion.",
                mitigation = "M1032: Multi-factor Authentication. Use MFA for all external access.",
                color = Color(0xFF00E676) // Green
            ),
            MitreTechnique(
                id = "T1204",
                name = "User Execution",
                tactic = "Execution",
                description = "An adversary may rely upon specific actions by a user in order to gain execution. This may be direct execution of malicious code or opening a file.",
                mitigation = "M1050: Exploit Protection. Use utility tools like browser extensions to block execution of malicious scripts.",
                color = Color(0xFF2979FF) // Blue
            )
        )
    }
}