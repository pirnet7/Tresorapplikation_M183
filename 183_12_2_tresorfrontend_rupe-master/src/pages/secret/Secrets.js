import '../../App.css';
import '../../css/Secrets.css';
import React, {useEffect, useState} from 'react';
import {getSecretsforUser} from "../../comunication/FetchSecrets";

/**
 * Secrets
 * @author Peter Rutschmann
 */
const Secrets = ({loginValues}) => {
    const [secrets, setSecrets] = useState([]);
    const [errorMessage, setErrorMessage] = useState('');

    useEffect(() => {
        const fetchSecrets = async () => {
            setErrorMessage('');
            if( ! loginValues.email){
                console.error('Secrets: No valid email, please do login first:' + loginValues);
                setErrorMessage("No valid email, please do login first.");
            } else {
                try {
                    const data = await getSecretsforUser({
                        email: loginValues.email,
                        encryptPassword: loginValues.password // loginValues.password is the master password
                    });
                    console.log("Fetched secrets data:", data);
                    if (Array.isArray(data)) {
                        setSecrets(data);
                    } else {
                        console.error('Fetched data is not an array:', data);
                        setSecrets([]);
                        setErrorMessage('Received unexpected data format for secrets.');
                    }
                } catch (error) {
                    console.error('Failed to fetch to server:', error.message);
                    setErrorMessage(error.message);
                }
            }
        };
        fetchSecrets();
    }, [loginValues]);

    // Group secrets by type
    const groupedSecrets = secrets.reduce((acc, secret) => {
        // Assuming secret.content is a JSON string that needs to be parsed
        let content;
        try {
            content = JSON.parse(secret.content);
        } catch (e) {
            console.error("Failed to parse secret content:", secret.content, e);
            // If content is not valid JSON, we can skip it or assign a default type
            return acc;
        }

        const type = content.type || ' '; // Assuming type is a field in the parsed content
        if (!acc[type]) {
            acc[type] = [];
        }
        acc[type].push({...secret, content}); // Store parsed content
        return acc;
    }, {});

    return (
        <main className="secrets-main">
            <h1>My Secrets</h1>
            {errorMessage && <p className="error-message">{errorMessage}</p>}

            {Object.keys(groupedSecrets).length > 0 ? (
                Object.entries(groupedSecrets).map(([type, secretsOfType]) => (
                    <section key={type} className="secret-type-section">
                        <h2>{type.charAt(0).toUpperCase() + type.slice(1)}</h2>
                        {secretsOfType.length > 0 ? (
                            <div className="secrets-grid">
                                {secretsOfType.map(secret => (
                                    <div key={secret.id} className="secret-card">
                                        {/* Adjust how you display content based on its structure */}
                                        {/* This is a generic way, you'll need to adapt it */}
                                        {Object.entries(secret.content).map(([key, value]) => {
                                            if (key === 'type') return null; // Don't display the type field again
                                            // Check if value is an object, if so stringify it, otherwise display directly
                                            const displayValue = typeof value === 'object' && value !== null ? JSON.stringify(value, null, 2) : value;
                                            return (
                                                <div key={key} className="secret-field">
                                                    <strong>{key.charAt(0).toUpperCase() + key.slice(1)}:</strong>
                                                    {key === 'password' || key === 'secretKey' || key === 'cvv' ? '******' : <pre>{displayValue}</pre>}
                                                </div>
                                            );
                                        })}
                                    </div>
                                ))}
                            </div>
                        ) : (
                            <p>No secrets of this type.</p>
                        )}
                    </section>
                ))
            ) : (
                !errorMessage && <p>No secrets available.</p>
            )}
        </main>
    );
};

export default Secrets;