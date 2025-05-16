import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {postSecret} from "../../comunication/FetchSecrets";

/**
 * NewCredential
 * @author Peter Rutschmann
 */
function NewCredential({loginValues}) {
    const initialState = {
        kindid: 1,
        kind:"credential",
        userName: "",
        password: "",
        url: ""
    };
    const [credentialValues, setCredentialValues] = useState(initialState);
    const [errorMessage, setErrorMessage] = useState('');

    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrorMessage('');
        console.log("Login values available in NewCredential:", loginValues) // For debugging
        try {
            // Prepare the data for the backend
            const title = credentialValues.url || "Credential"; // Use URL as title, or default
            const email = loginValues.email; 
            const encryptPassword = loginValues.password; // This is the user's master password
            
            // The 'content' for the backend is the credentialValues object itself
            const contentForBackend = credentialValues;

            if (!email || !encryptPassword) {
                console.error('Email or master password is missing from loginValues.');
                setErrorMessage('User session error. Please log in again.');
                return;
            }

            await postSecret({
                title: title,
                email: email,
                encryptPassword: encryptPassword,
                content: contentForBackend 
            });
            setCredentialValues(initialState);
            navigate('/secret/secrets');
        } catch (error) {
            console.error('Failed to fetch to server:', error.message);
            setErrorMessage(error.message);
        }
    };

    return (
        <div>
            <h2>Add new credential secret</h2>
            <form onSubmit={handleSubmit}>
                <section>
                    <aside>
                        <div>
                            <label>username:</label>
                            <input
                                type="text"
                                value={credentialValues.userName}
                                onChange={(e) =>
                                    setCredentialValues(prevValues => ({...prevValues, userName: e.target.value}))}
                                required
                                placeholder="Please enter username"
                            />
                        </div>
                        <div>
                            <label>password:</label>
                            <input
                                type="text"
                                value={credentialValues.password}
                                onChange={(e) =>
                                    setCredentialValues(prevValues => ({...prevValues, password: e.target.value}))}
                                required
                                placeholder="Please enter password"
                            />
                        </div>
                        <div>
                            <label>url:</label>
                            <input
                                type="text"
                                value={credentialValues.url}
                                onChange={(e) =>
                                    setCredentialValues(prevValues => ({...prevValues, url: e.target.value}))}
                                required
                                placeholder="Please enter url"
                            />
                        </div>
                        <button type="submit">save secret</button>
                        {errorMessage && <p style={{color: 'red'}}>{errorMessage}</p>}
                    </aside>
                </section>
            </form>
        </div>
    );
}

export default NewCredential;
