package com.example.secura.modulesscreen.chatbot;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secura.R;
import com.example.secura.splashloginregister.DatabaseHelper;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatbotActivity extends AppCompatActivity {

    private static final String PREF_NAME = "ChatbotPrefs";
    private static final String PREF_KEY_FIRST_RUN = "isFirstRun";
    private static final int TYPING_DELAY = 1500; // Delay for typing animation in milliseconds
    private static final int TYPING_SPEED = 50; // Delay between characters in milliseconds

    private RecyclerView chatRecyclerView;
    private TextInputEditText messageInput;
    private ImageButton sendButton;
    private ImageButton clearChatButton;
    private ChipGroup suggestionChipGroup;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessageList;
    private DatabaseHelper databaseHelper;
    private String userName;

    // Hardcoded cybersecurity keywords and their information
    private final Map<String, String> keywordDatabase = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        // Initialize UI components
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Cyber Chatbot");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        messageInput = findViewById(R.id.message_input_edit_text);
        sendButton = findViewById(R.id.send_button);
        clearChatButton = findViewById(R.id.clear_chat_button);
        suggestionChipGroup = findViewById(R.id.suggestion_chip_group);

        // Initialize chat message list and adapter
        chatMessageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessageList);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // Initialize keyword database
        populateKeywordDatabase();

        // Get the user's name from the database
        databaseHelper = new DatabaseHelper(this);
        fetchUserNameFromDatabase();

        // Check if this is the first time the chatbot is opened in this session
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean(PREF_KEY_FIRST_RUN, true);

        if (isFirstRun) {
            // First run: Greet the user with a delay and typing animation
            prefs.edit().putBoolean(PREF_KEY_FIRST_RUN, false).apply();
            greetUserWithTypingAnimation();
        } else {
            // Not the first run: Greet the user instantly
            greetUserWithTypingAnimation();
        }

        // Set up listeners
        setupListeners();
    }

    private void populateKeywordDatabase() {
        // Hardcoded keywords and information. You can add more here.
        keywordDatabase.put("phishing", "Phishing is a type of social engineering where an attacker sends a fraudulent message designed to trick a person into revealing sensitive information.");
        keywordDatabase.put("malware", "Malware is an umbrella term for any malicious software, including viruses, spyware, and ransomware, designed to damage or gain unauthorized access to computer systems.");
        keywordDatabase.put("firewall", "A firewall is a network security system that monitors and controls incoming and outgoing network traffic based on predetermined security rules.");
        keywordDatabase.put("encryption", "Encryption is the process of converting information or data into a code to prevent unauthorized access.");
        keywordDatabase.put("vpn", "A VPN (Virtual Private Network) gives you online privacy and anonymity by creating a private network from a public internet connection.");
        keywordDatabase.put("ddos", "A DDoS (Distributed Denial-of-Service) attack is a malicious attempt to disrupt the normal traffic of a targeted server, service or network by overwhelming it with a flood of Internet traffic.");
        keywordDatabase.put("password", "A strong password is a unique sequence of characters that is difficult for others to guess or for computers to crack.");
        keywordDatabase.put("antivirus", "Antivirus software is a program designed to detect, prevent, and remove malicious software.");
        keywordDatabase.put("spyware", "Spyware is a type of malicious software that is designed to enter your computer, gather data about you, and forward it to a third party without your consent.");
        keywordDatabase.put("ransomware", "Ransomware is a type of malware that threatens to publish the victim's data or perpetually block access to it unless a ransom is paid.");
        keywordDatabase.put("trojan", "A Trojan is a type of malware that is disguised as legitimate software to trick users into downloading it.");
        keywordDatabase.put("virus", "A computer virus is a type of malicious software that, when executed, replicates by modifying other computer programs and inserting its own code.");
        keywordDatabase.put("worm", "A computer worm is a standalone malware computer program that replicates itself to spread to other computers.");
        keywordDatabase.put("botnet", "A botnet is a network of private computers infected with malicious software and controlled as a group without the owners' knowledge.");
        keywordDatabase.put("social engineering", "Social engineering is the use of deception to manipulate individuals into divulging confidential or personal information.");
        keywordDatabase.put("keylogger", "A keylogger is a type of surveillance technology used to monitor and record each keystroke typed on a specific computer's keyboard.");
        keywordDatabase.put("cyberattack", "A cyberattack is a malicious and deliberate attempt by an individual or organization to breach the information system of another individual or organization.");
        keywordDatabase.put("authentication", "Authentication is the process of verifying a person's identity before allowing them access to a system or data.");
        keywordDatabase.put("authorization", "Authorization is the process of granting or denying access to a network resource.");
        keywordDatabase.put("two-factor authentication", "Two-factor authentication (2FA) is a security process in which the user provides two different authentication factors to verify themselves.");
        keywordDatabase.put("biometrics", "Biometrics are unique physical characteristics, such as fingerprints, used to verify identity.");
        keywordDatabase.put("vulnerability", "A vulnerability is a weakness in a system or network that can be exploited by an attacker.");
        keywordDatabase.put("exploit", "An exploit is a piece of software, data, or sequence of commands that takes advantage of a bug or vulnerability to cause unintended behavior.");
        keywordDatabase.put("patch", "A software patch is a set of changes to a computer program or its supporting data designed to update, fix, or improve it.");
        keywordDatabase.put("zero-day attack", "A zero-day attack is a cyberattack that occurs on the same day a software vulnerability is publicly disclosed.");
        keywordDatabase.put("packet sniffing", "Packet sniffing is the act of capturing data packets as they travel across a computer network.");
        keywordDatabase.put("man-in-the-middle attack", "A man-in-the-middle (MITM) attack is an attack where the attacker secretly relays and alters the communication between two parties who believe they are directly communicating with each other.");
        keywordDatabase.put("brute-force attack", "A brute-force attack is a trial-and-error method used to decode encrypted data such as passwords by trying every possible combination.");
        keywordDatabase.put("spoofing", "Spoofing is a malicious practice in which communication is sent from an unknown source disguised as a source known to the receiver.");
        keywordDatabase.put("pharming", "Pharming is a cybercrime similar to phishing, where a user is redirected to a fraudulent website without their knowledge or consent.");
        keywordDatabase.put("cookie", "An HTTP cookie is a small piece of data sent from a website and stored on the user's computer by their web browser.");
        keywordDatabase.put("threat", "A threat is a potential danger that might exploit a vulnerability to breach security and cause possible harm.");
        keywordDatabase.put("risk", "Risk is the potential for loss or damage when a threat exploits a vulnerability.");
        keywordDatabase.put("cybersecurity", "Cybersecurity is the practice of protecting systems, networks, and programs from digital attacks.");
        keywordDatabase.put("incident", "An incident is an event that compromises the confidentiality, integrity, or availability of an information asset.");
        keywordDatabase.put("compliance", "Compliance in cybersecurity refers to adherence to laws, regulations, and standards that govern data protection.");
        keywordDatabase.put("forensics", "Cyber forensics is the application of investigation and analysis techniques to gather and preserve evidence from a computing device in a way that is suitable for presentation in a court of law.");
        keywordDatabase.put("breach", "A data breach is an incident where sensitive, confidential or protected data is stolen or taken from a system without the knowledge or authorization of the system's owner.");
        keywordDatabase.put("social media security", "Social media security involves protecting the information on social media platforms from unauthorized access and misuse.");
        keywordDatabase.put("access control", "Access control is a security technique that regulates who or what can view or use resources in a computing environment.");
        keywordDatabase.put("public key infrastructure", "Public Key Infrastructure (PKI) is a set of roles, policies, and procedures needed to create, manage, distribute, use, store, and revoke digital certificates and manage public-key encryption.");
        keywordDatabase.put("digital signature", "A digital signature is a mathematical scheme for verifying the authenticity of digital messages or documents.");
        keywordDatabase.put("cryptography", "Cryptography is the practice and study of techniques for secure communication in the presence of third parties.");
        keywordDatabase.put("plaintext", "Plaintext is unencrypted information, or data that is readable without any special conversion.");
        keywordDatabase.put("ciphertext", "Ciphertext is the result of encryption performed on plaintext.");
        keywordDatabase.put("hacker", "A hacker is an individual who uses computer, networking or other skills to overcome a technical problem.");
        keywordDatabase.put("black hat hacker", "A black hat hacker is an individual who uses their skills for malicious or illegal purposes.");
        keywordDatabase.put("white hat hacker", "A white hat hacker is an ethical security hacker who uses their skills to find and fix vulnerabilities in systems.");
        keywordDatabase.put("gray hat hacker", "A gray hat hacker is an individual who finds system vulnerabilities without permission but discloses them publicly to pressure the organization to fix the issue.");
        keywordDatabase.put("cyber espionage", "Cyber espionage is the act or practice of obtaining secrets without the permission of the holder of the information, usually for political or military advantage, using methods on the Internet.");
        keywordDatabase.put("insider threat", "An insider threat is a malicious threat to an organization that comes from people within the organization, such as employees, former employees, contractors, or business partners.");
        keywordDatabase.put("privilege escalation", "Privilege escalation is the act of exploiting a bug, a design flaw or a configuration oversight in a software or system to gain elevated access to resources.");
        keywordDatabase.put("denial-of-service attack", "A denial-of-service (DoS) attack is a single attacker's attempt to make a machine or network resource unavailable to its intended users.");
        keywordDatabase.put("cross-site scripting", "Cross-site scripting (XSS) is a type of security vulnerability typically found in web applications that enables attackers to inject client-side scripts into web pages viewed by other users.");
        keywordDatabase.put("sql injection", "SQL injection is a web security vulnerability that allows an attacker to interfere with the queries that an application makes to its database.");
        keywordDatabase.put("cyber hygiene", "Cyber hygiene is a set of practices and habits that users of computers and other devices can adopt to improve their online security.");
        keywordDatabase.put("security audit", "A security audit is a systematic evaluation of the security of a company's information system by measuring how well it conforms to a set of established criteria.");
        keywordDatabase.put("penetration testing", "Penetration testing is a simulated cyberattack against your computer system to check for exploitable vulnerabilities.");
        keywordDatabase.put("vulnerability scanning", "Vulnerability scanning is an inspection of the potential points of exploit on a network or computer to identify security holes.");
        keywordDatabase.put("ethical hacking", "Ethical hacking is an authorized attempt to gain unauthorized access to a computer system, application, or data.");
        keywordDatabase.put("risk assessment", "A risk assessment is the process of identifying, analyzing, and evaluating risks to an organization's assets.");
        keywordDatabase.put("information security", "Information security is the practice of protecting information by mitigating information risks.");
        keywordDatabase.put("endpoint security", "Endpoint security is the practice of securing endpoints or entry points of end-user devices such as desktops, laptops, and mobile devices from being exploited by malicious actors and campaigns.");
        keywordDatabase.put("cloud security", "Cloud security is a set of policies, controls, procedures, and technologies that work together to protect cloud-based systems, data, and infrastructure.");
        keywordDatabase.put("network security", "Network security consists of the policies, processes, and practices adopted to prevent, detect, and monitor unauthorized access, misuse, modification, or denial of a computer network and its resources.");
        keywordDatabase.put("web security", "Web security is the practice of defending websites and web applications from threats that can compromise their security.");
        keywordDatabase.put("mobile security", "Mobile security is the protection of mobile devices and the data they contain from threats like malware and unauthorized access.");
        keywordDatabase.put("internet of things security", "IoT security is the practice of protecting IoT devices and the networks they connect to from cyberattacks.");
        keywordDatabase.put("security information and event management", "SIEM (Security Information and Event Management) is a software solution that aggregates and analyzes activity from many different resources across your entire IT infrastructure.");
        keywordDatabase.put("intrusion detection system", "An Intrusion Detection System (IDS) is a device or software application that monitors a network or system for malicious activity or policy violations.");
        keywordDatabase.put("intrusion prevention system", "An Intrusion Prevention System (IPS) is a network security appliance that monitors network and/or system activities for malicious or unwanted behavior and can react to block or prevent those activities.");
        keywordDatabase.put("data loss prevention", "DLP (Data Loss Prevention) is a set of tools and processes used to ensure that sensitive data is not lost, misused, or accessed by unauthorized users.");
        keywordDatabase.put("public key", "A public key is a cryptographic key that can be obtained and used by anyone to encrypt messages intended for a particular recipient.");
        keywordDatabase.put("private key", "A private key is a cryptographic key that is known only to the owner and is used to decrypt messages encrypted with the corresponding public key.");
        keywordDatabase.put("certificate authority", "A Certificate Authority (CA) is an entity that issues digital certificates, which are data files that verify the authenticity of an individual or organization online.");
        keywordDatabase.put("security policy", "A security policy is a document that defines the security controls and requirements for an organization's information systems.");
        keywordDatabase.put("acceptable use policy", "An acceptable use policy (AUP) is a document that outlines a set of rules and guidelines for how a computer network, website, or other resource can be used.");
        keywordDatabase.put("privacy policy", "A privacy policy is a legal document that discloses some or all of the ways a party gathers, uses, discloses, and manages a customer or client's data.");
        keywordDatabase.put("asset", "An asset is any data, device, or other component of the information system that has value to the organization.");
        keywordDatabase.put("asset management", "Asset management is the process of tracking and managing the assets within an organization's IT infrastructure.");
        keywordDatabase.put("business continuity plan", "A business continuity plan (BCP) is a document that outlines how an organization will continue to function after a disaster or serious disruption.");
        keywordDatabase.put("disaster recovery plan", "A disaster recovery plan (DRP) is a document that outlines how to recover a business's IT infrastructure after a disaster.");
        keywordDatabase.put("incident response plan", "An incident response plan is a document that outlines the steps to be taken when a cybersecurity incident occurs.");
        keywordDatabase.put("threat intelligence", "Threat intelligence is the analysis of information about existing and potential threats to an organization's assets.");
        keywordDatabase.put("cyber threat intelligence", "Cyber threat intelligence is the analysis of data about cyber threats and threat actors to help organizations make informed decisions about their security.");
        keywordDatabase.put("vulnerability management", "Vulnerability management is the cyclical practice of identifying, classifying, prioritizing, remediating, and mitigating software vulnerabilities.");
        keywordDatabase.put("security operations center", "A Security Operations Center (SOC) is a centralized unit that deals with security issues on an organizational and technical level.");
        keywordDatabase.put("managed security service provider", "A Managed Security Service Provider (MSSP) is a company that provides outsourced monitoring and management of security devices and systems.");
        keywordDatabase.put("software as a service", "SaaS (Software as a Service) is a software distribution model in which a third-party provider hosts applications and makes them available to customers over the Internet.");
        keywordDatabase.put("platform as a service", "PaaS (Platform as a Service) is a cloud computing model where a third-party provider delivers hardware and software tools to users over the Internet.");
        keywordDatabase.put("infrastructure as a service", "IaaS (Infrastructure as a Service) is a form of cloud computing that provides virtualized computing resources over the Internet.");
        keywordDatabase.put("security as a service", "SECaaS (Security as a Service) is a business model in which a service provider offers security services to an organization for a fee.");
        keywordDatabase.put("compliance as a service", "CaaS (Compliance as a Service) is a business model in which a service provider offers compliance services to an organization for a fee.");
        keywordDatabase.put("general data protection regulation", "The GDPR (General Data Protection Regulation) is a legal framework that sets guidelines for the collection and processing of personal information from individuals who live in the European Union.");
        keywordDatabase.put("health insurance portability and accountability act", "HIPAA (Health Insurance Portability and Accountability Act) is a U.S. law that protects the privacy of patients' medical information.");
        keywordDatabase.put("payment card industry data security standard", "PCI DSS (Payment Card Industry Data Security Standard) is a set of security standards designed to ensure that all companies that accept, process, store, or transmit credit card information maintain a secure environment.");
        keywordDatabase.put("zero trust", "Zero trust is a security model based on the principle of 'never trust, always verify'.");
        keywordDatabase.put("least privilege", "The principle of least privilege is the concept that a user or process should only be given the minimum level of access and permissions necessary to perform its job.");
        keywordDatabase.put("defense in depth", "Defense in depth is a cybersecurity strategy in which multiple layers of security are used to protect an organization's assets.");
        keywordDatabase.put("security by design", "Security by design is a software development approach where security is considered from the very beginning of the design phase.");
        keywordDatabase.put("buffer overflow", "A buffer overflow is an anomaly where a program, while writing data to a buffer, overruns the buffer's boundary and overwrites adjacent memory locations.");
        keywordDatabase.put("command injection", "Command injection is a technique used by attackers to execute arbitrary commands on a host operating system.");
        keywordDatabase.put("cookie poisoning", "Cookie poisoning is an attack where an attacker modifies the contents of a cookie to exploit a web application.");
        keywordDatabase.put("directory traversal", "Directory traversal is an HTTP attack which allows attackers to access restricted directories and files on a web server.");
        keywordDatabase.put("cross-site request forgery", "Cross-site request forgery (CSRF) is a type of malicious exploit of a website where unauthorized commands are transmitted from a user that the web application trusts.");
        keywordDatabase.put("session hijacking", "Session hijacking is the exploitation of a valid computer session to gain unauthorized access to information or services in a computer system.");
        keywordDatabase.put("malvertising", "Malvertising is the use of online advertising to spread malware.");
        keywordDatabase.put("watering hole attack", "A watering hole attack is a security exploit in which the attacker infects a website that is frequently visited by the targeted group of users.");
        keywordDatabase.put("smishing", "Smishing is a form of phishing that uses text messages (SMS) to trick victims into revealing personal information.");
        keywordDatabase.put("vishing", "Vishing is a form of phishing that uses telephone calls to trick victims into revealing personal information.");
        keywordDatabase.put("tailgating", "Tailgating is the act of an unauthorized person gaining access to a restricted area by following an authorized person through a security checkpoint.");
        keywordDatabase.put("shouldering", "Shouldering is the act of an attacker watching a user's screen or keyboard to steal sensitive information like passwords or PINs.");
        keywordDatabase.put("piggybacking", "Piggybacking is the act of a user gaining unauthorized access to a computer network or system by using an authorized user's credentials.");
        keywordDatabase.put("dumpster diving", "Dumpster diving is the act of sifting through garbage to find discarded information that can be used to launch a cyberattack.");
        keywordDatabase.put("whaling", "Whaling is a form of phishing that targets high-profile individuals, such as CEOs or CFOs, in an organization.");
        keywordDatabase.put("spear phishing", "Spear phishing is a targeted form of phishing that focuses on specific individuals or organizations.");
        keywordDatabase.put("pretexting", "Pretexting is a form of social engineering where an attacker creates a fabricated scenario or 'pretext' to manipulate a victim into divulging information or performing an action.");
        keywordDatabase.put("credential stuffing", "Credential stuffing is a type of cyberattack in which attackers use lists of stolen usernames and passwords to gain access to other accounts.");
        keywordDatabase.put("adware", "Adware is a type of malicious software that automatically delivers unwanted advertisements to a user's computer.");
        keywordDatabase.put("rootkit", "A rootkit is a type of malicious software designed to give an attacker remote access and control over a computer system without the user's knowledge.");
        keywordDatabase.put("logic bomb", "A logic bomb is a piece of code intentionally inserted into a software system that will set off a malicious function when specified conditions are met.");
        keywordDatabase.put("backdoor", "A backdoor is a method of bypassing normal authentication or encryption in a computer system.");
        keywordDatabase.put("data integrity", "Data integrity is the maintenance of data consistency, accuracy, and trustworthiness over its entire life cycle.");
        keywordDatabase.put("data availability", "Data availability is the principle that data and services should be available to authorized users when needed.");
        keywordDatabase.put("confidentiality", "Confidentiality is the principle that information should not be disclosed to unauthorized individuals or systems.");
        keywordDatabase.put("risk management", "Risk management is the process of identifying, assessing, and prioritizing risks to an organization's assets.");
        keywordDatabase.put("threat modeling", "Threat modeling is a process by which potential threats, such as structural vulnerabilities, can be identified, enumerated, and prioritized.");
        keywordDatabase.put("security controls", "Security controls are safeguards or countermeasures to avoid, detect, counteract, or minimize security risks to physical property, information, computer systems, or other assets.");
        keywordDatabase.put("administrative controls", "Administrative controls are policies, procedures, and standards that guide the behavior of employees and management to protect an organization's assets.");
        keywordDatabase.put("technical controls", "Technical controls are security controls that are implemented through technology, such as firewalls and encryption.");
        keywordDatabase.put("physical controls", "Physical controls are security controls that are tangible, such as locks, fences, and security guards.");
        keywordDatabase.put("cryptographic hash", "A cryptographic hash function is an algorithm that takes an arbitrary block of data and returns a fixed-size bit string, which is the hash value.");
        keywordDatabase.put("digital certificate", "A digital certificate is a data file that verifies the authenticity of a website, user, or server.");
        keywordDatabase.put("secure socket layer", "SSL (Secure Sockets Layer) is a cryptographic protocol designed to provide communication security over a computer network.");
        keywordDatabase.put("transport layer security", "TLS (Transport Layer Security) is the successor to SSL and is used to encrypt communication between a web server and a web browser.");
        keywordDatabase.put("public key cryptography", "Public key cryptography, or asymmetric cryptography, is a cryptographic system that uses pairs of keys: public keys which may be disseminated widely, and private keys which are known only to the owner.");
        keywordDatabase.put("symmetric cryptography", "Symmetric cryptography is a method of encryption where the same key is used to encrypt and decrypt data.");
        keywordDatabase.put("hash", "A hash is a function that maps data of arbitrary size to data of a fixed size.");
        keywordDatabase.put("blockchain", "Blockchain is a decentralized, distributed, and unchangeable ledger of transactions.");
        keywordDatabase.put("distributed ledger technology", "DLT (Distributed Ledger Technology) is a digital system for recording the transaction of assets in which the transactions and their details are recorded in multiple places at the same time.");
        keywordDatabase.put("smart contract", "A smart contract is a self-executing contract with the terms of the agreement between a buyer and seller being directly written into lines of code.");
        keywordDatabase.put("cryptocurrency", "A cryptocurrency is a digital or virtual currency that is secured by cryptography.");
        keywordDatabase.put("bitcoin", "Bitcoin is a decentralized digital currency without a central bank or single administrator.");
        keywordDatabase.put("ethereum", "Ethereum is a decentralized, open-source blockchain with smart contract functionality.");
        keywordDatabase.put("non-fungible token", "An NFT (Non-Fungible Token) is a unique and non-interchangeable unit of data stored on a digital ledger.");
        keywordDatabase.put("tokenization", "Tokenization is the process of substituting a sensitive data element with a non-sensitive equivalent, known as a token.");
        keywordDatabase.put("data masking", "Data masking is a data security technique in which a dataset's sensitive data is hidden by replacing it with realistic but false information.");
        keywordDatabase.put("database security", "Database security is a broad area of information security that deals with the controls used to protect a database against malicious attacks.");
        keywordDatabase.put("application security", "Application security is the process of developing, adding, and testing security features within applications to prevent vulnerabilities and unauthorized access.");
        keywordDatabase.put("operating system security", "Operating system security is the process of protecting an operating system from unauthorized access and misuse.");
        keywordDatabase.put("physical security", "Physical security is the protection of people, assets, and data from physical actions and events that could cause serious damage or loss.");
        keywordDatabase.put("personnel security", "Personnel security is the practice of protecting an organization from the unauthorized disclosure of information by its employees or contractors.");
        keywordDatabase.put("contingency plan", "A contingency plan is a plan designed to take a possible future event or circumstance into account.");
        keywordDatabase.put("supply chain security", "Supply chain security is the practice of protecting a supply chain from theft, vandalism, and other malicious activities.");
        keywordDatabase.put("threat hunting", "Threat hunting is the proactive process of searching for threats that are lurking undetected in a network.");
        keywordDatabase.put("red team", "A red team is a group of ethical hackers who simulate attacks on an organization's systems to find vulnerabilities.");
        keywordDatabase.put("blue team", "A blue team is a group of security professionals who defend an organization's systems from cyberattacks.");
        keywordDatabase.put("purple team", "A purple team is a group that combines the functions of the red and blue teams to improve the organization's security posture.");
        keywordDatabase.put("honeypot", "A honeypot is a security mechanism that is used to detect, deflect, or, in some manner, counteract attempts at unauthorized use of information systems.");
        keywordDatabase.put("sandbox", "A sandbox is an isolated testing environment that enables users to run programs or open files without affecting the application, system, or platform on which they run.");
        keywordDatabase.put("firewall rule", "A firewall rule is a statement in a firewall that defines the conditions under which network traffic is allowed or denied.");
        keywordDatabase.put("demilitarized zone", "A DMZ (Demilitarized Zone) is a physical or logical subnetwork that contains and exposes an organization's external-facing services to an untrusted, usually larger, network such as the Internet.");
        keywordDatabase.put("router", "A router is a networking device that forwards data packets between computer networks.");
        keywordDatabase.put("switch", "A switch is a networking device that connects devices on a computer network by using packet switching to receive, process, and forward data to the destination device.");
        keywordDatabase.put("server", "A server is a computer program or a device that provides functionality for other programs or devices, called 'clients'.");
        keywordDatabase.put("client", "A client is a piece of computer hardware or software that accesses a service made available by a server.");
        keywordDatabase.put("database", "A database is an organized collection of data, generally stored and accessed electronically from a computer system.");
        keywordDatabase.put("application programming interface", "An API (Application Programming Interface) is a set of rules and protocols for building and interacting with software applications.");
        keywordDatabase.put("phishing kit", "A phishing kit is a collection of web pages, scripts, and software that an attacker can use to create a phishing website.");
        keywordDatabase.put("business email compromise", "BEC (Business Email Compromise) is a type of phishing attack where an attacker impersonates a company executive to trick an employee into transferring money or sensitive data.");
        keywordDatabase.put("dns poisoning", "DNS poisoning is a form of computer security hacking in which corrupt Domain Name System (DNS) data is introduced into the DNS resolver's cache, causing the name server to return an incorrect IP address.");
        keywordDatabase.put("watermarking", "Watermarking is the process of embedding information into a digital medium, such as a video, audio, or image file.");
        keywordDatabase.put("steganography", "Steganography is the practice of concealing a file, message, image, or video within another file, message, image, or video.");
        keywordDatabase.put("honeynets", "A honeynet is a network of honeypots used to monitor and analyze attacker behavior.");
        keywordDatabase.put("security orchestration, automation and response", "SOAR (Security Orchestration, Automation and Response) is a stack of software tools that helps organizations manage and respond to security incidents.");
        keywordDatabase.put("zero-day vulnerability", "A zero-day vulnerability is a software vulnerability that is unknown to the vendor and has no patch available.");
        keywordDatabase.put("malicious code", "Malicious code is any code in any part of a software system or script that is intended to cause undesired effects, security breaches, or damage to a system.");
        keywordDatabase.put("data at rest", "Data at rest is data that is stored on a device and is not in transit.");
        keywordDatabase.put("data in transit", "Data in transit is data that is being transmitted over a network.");
        keywordDatabase.put("data in use", "Data in use is data that is currently being processed or used by a computer system.");
        keywordDatabase.put("confidentiality, integrity, and availability", "The CIA triad (Confidentiality, Integrity, and Availability) is a security model that guides information security policies.");
        keywordDatabase.put("risk acceptance", "Risk acceptance is a risk management strategy in which an organization decides to accept the potential risk of a security incident.");
        keywordDatabase.put("risk avoidance", "Risk avoidance is a risk management strategy in which an organization takes steps to avoid a potential risk.");
        keywordDatabase.put("risk mitigation", "Risk mitigation is a risk management strategy in which an organization takes steps to reduce the likelihood or impact of a potential risk.");
        keywordDatabase.put("risk transfer", "Risk transfer is a risk management strategy in which an organization transfers the risk to a third party, such as by purchasing insurance.");
        keywordDatabase.put("threat actor", "A threat actor is a person or group that is responsible for a security incident.");
        keywordDatabase.put("advanced persistent threat", "APT (Advanced Persistent Threat) is a stealthy and continuous computer hacking process, often targeting a specific entity.");
        keywordDatabase.put("script kiddie", "A script kiddie is an unskilled individual who uses scripts or programs developed by others to attack computer systems.");
        keywordDatabase.put("hacktivist", "A hacktivist is a person who uses hacking to promote a political agenda.");
        keywordDatabase.put("nation-state actor", "A nation-state actor is a government-sponsored group that engages in cyberattacks for political or military purposes.");
        keywordDatabase.put("ransomware as a service", "RaaS (Ransomware as a Service) is a business model in which a ransomware operator provides their malicious code and infrastructure to other attackers for a fee.");
        keywordDatabase.put("malware as a service", "MaaS (Malware as a Service) is a business model in which a malware operator provides their malicious code and infrastructure to other attackers for a fee.");
    }

    private void fetchUserNameFromDatabase() {
        // Fetch the user's name from the database
        // NOTE: This is a conceptual implementation. In a real app, you would
        // get the logged-in user's username from a session manager or intent extras.
        String loggedInUsername = "test_user"; // Replace with actual logged-in username
        Cursor cursor = databaseHelper.getUserDetails(loggedInUsername);
        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(DatabaseHelper.COL_NAME);
            if (nameIndex != -1) {
                userName = cursor.getString(nameIndex);
            } else {
                userName = ""; // Fallback name
            }
            cursor.close();
        } else {
            userName = ""; // Fallback name
        }
    }

    private void greetUserWithTypingAnimation() {
        // Add "typing..." message first
        addTypingMessage();

        // Use a Handler to simulate a delay before showing the full greeting
        new Handler().postDelayed(() -> {
            // Remove the "typing..." message
            chatMessageList.remove(chatMessageList.size() - 1);
            chatAdapter.notifyItemRemoved(chatMessageList.size());

            // Add the final greeting
            String greeting = "Hi, " + userName + "! I am Cyber Chatbot, a smart assistant to help you understand cybersecurity terms. What would you like to learn today? Type a term related to Cyber Security.";
            addBotMessageWithTypingEffect(greeting);
        }, TYPING_DELAY);
    }

    private void greetUserInstantly() {
        String greeting = "Hi, " + userName + "! I am Cyber Chatbot, a smart assistant to help you understand cybersecurity terms. What would you like to learn today? Type a term related to Cyber Security.";
        addBotMessage(greeting);
    }

    private void setupListeners() {
        // Send button click listener
        sendButton.setOnClickListener(v -> sendMessage());

        // Clear chat button click listener
        clearChatButton.setOnClickListener(v -> {
            clearChat();
            greetUserWithTypingAnimation();
        });

        // Input field text change listener for suggestions
        messageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                showSuggestions(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not used
            }
        });
    }

    private void showSuggestions(String input) {
        suggestionChipGroup.removeAllViews();
        if (input.trim().isEmpty()) {
            suggestionChipGroup.setVisibility(View.GONE);
            return;
        }

        String lowerCaseInput = input.trim().toLowerCase();
        List<String> matchingKeywords = new ArrayList<>();
        for (String keyword : keywordDatabase.keySet()) {
            if (keyword.contains(lowerCaseInput)) {
                matchingKeywords.add(keyword);
            }
        }
        Collections.sort(matchingKeywords);

        if (!matchingKeywords.isEmpty()) {
            suggestionChipGroup.setVisibility(View.VISIBLE);
            for (String keyword : matchingKeywords) {
                Chip chip = new Chip(this);
                chip.setText(keyword);
                chip.setChipBackgroundColorResource(R.color.blue_light);
                chip.setTextColor(getResources().getColor(R.color.white));
                chip.setOnClickListener(v -> {
                    messageInput.setText(keyword);
                    suggestionChipGroup.setVisibility(View.GONE);
                });
                suggestionChipGroup.addView(chip);
            }
        } else {
            suggestionChipGroup.setVisibility(View.GONE);
        }
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }

        // Add user message to the chat
        addUserMessage("User: " + messageText);
        messageInput.setText("");
        suggestionChipGroup.setVisibility(View.GONE);

        // Get chatbot reply and add it with a typing animation
        String botReply = getBotReply(messageText);
        addTypingMessage(); // Add "typing..." placeholder
        new Handler().postDelayed(() -> {
            // Remove the "typing..." message
            chatMessageList.remove(chatMessageList.size() - 1);
            chatAdapter.notifyItemRemoved(chatMessageList.size());

            // Add the final response
            addBotMessageWithTypingEffect(botReply);
        }, TYPING_DELAY);
    }

    private void addUserMessage(String text) {
        chatMessageList.add(new ChatMessage(userName + "" + text, ChatMessage.TYPE_USER));
        chatAdapter.notifyItemInserted(chatMessageList.size() - 1);
        chatRecyclerView.scrollToPosition(chatMessageList.size() - 1);
    }

    private void addBotMessage(String text) {
        chatMessageList.add(new ChatMessage("Chatbot: " + text, ChatMessage.TYPE_CHATBOT));
        chatAdapter.notifyItemInserted(chatMessageList.size() - 1);
        chatRecyclerView.scrollToPosition(chatMessageList.size() - 1);
    }

    private void addTypingMessage() {
        // Add a temporary "typing..." message to simulate the chatbot typing
        chatMessageList.add(new ChatMessage("Chatbot: Typing...", ChatMessage.TYPE_CHATBOT));
        chatAdapter.notifyItemInserted(chatMessageList.size() - 1);
        chatRecyclerView.scrollToPosition(chatMessageList.size() - 1);
    }

    private void addBotMessageWithTypingEffect(String fullText) {
        final StringBuilder builder = new StringBuilder();
        final Handler handler = new Handler();
        final int[] index = {0};

        // Add an empty message placeholder
        chatMessageList.add(new ChatMessage("", ChatMessage.TYPE_CHATBOT));
        chatAdapter.notifyItemInserted(chatMessageList.size() - 1);
        chatRecyclerView.scrollToPosition(chatMessageList.size() - 1);

        final int messageIndex = chatMessageList.size() - 1;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (index[0] < fullText.length()) {
                    builder.append(fullText.charAt(index[0]));
                    chatMessageList.get(messageIndex).setText("Chatbot: " + builder.toString());
                    chatAdapter.notifyItemChanged(messageIndex);
                    index[0]++;
                    handler.postDelayed(this, TYPING_SPEED);
                } else {
                    chatRecyclerView.scrollToPosition(chatMessageList.size() - 1);
                }
            }
        }, TYPING_SPEED);
    }

    private String getBotReply(String userMessage) {
        String lowerCaseMessage = userMessage.toLowerCase().trim();
        String reply = keywordDatabase.get(lowerCaseMessage);
        if (reply != null) {
            return reply;
        } else {
            return "I am sorry, I don't have information on that term. Please try another cyber security term.";
        }
    }

    private void clearChat() {
        chatMessageList.clear();
        chatAdapter.notifyDataSetChanged();
        // Reset the "first run" preference to ensure the greeting is shown again
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(PREF_KEY_FIRST_RUN, true).apply();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}