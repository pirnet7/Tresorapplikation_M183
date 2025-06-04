import React, { useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import {postUser} from "../../comunication/FetchUser";
import { useGoogleReCaptcha } from 'react-google-recaptcha-v3';

/**
 * RegisterUser
 * @author Peter Rutschmann
 */
function RegisterUser({loginValues, setLoginValues}) {
    const navigate = useNavigate();

    const initialState = {
        firstName: "",
        lastName: "",
        email: "",
        password: "",
        passwordConfirmation: "",
        errorMessage: ""
    };
    const [credentials, setCredentials] = useState(initialState);
    const [errorMessage, setErrorMessage] = useState('');
    const { executeRecaptcha } = useGoogleReCaptcha();
    const [passwordCriteriaStatus, setPasswordCriteriaStatus] = useState({
        minLength: false,
        hasUpperCase: false,
        hasLowerCase: false,
        hasDigit: false,
        hasSpecialChar: false,
    });

    const updatePasswordCriteria = (password) => {
        const criteria = {
            minLength: password.length >= 8,
            hasUpperCase: /[A-Z]/.test(password),
            hasLowerCase: /[a-z]/.test(password),
            hasDigit: /[0-9]/.test(password),
            hasSpecialChar: /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]+/.test(password),
        };
        setPasswordCriteriaStatus(criteria);
        return criteria; // Return for immediate use if needed
    };

    const isPasswordStrong = (password) => {
        // Re-evaluates criteria for submission; does not rely on state for logic here.
        const criteria = {
            minLength: password.length >= 8,
            hasUpperCase: /[A-Z]/.test(password),
            hasLowerCase: /[a-z]/.test(password),
            hasDigit: /[0-9]/.test(password),
            hasSpecialChar: /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]+/.test(password),
        };
        return Object.values(criteria).every(status => status);
    };

    const handleSubmit = useCallback(async (e) => {
        e.preventDefault();
        setErrorMessage('');

        if (!executeRecaptcha) {
            setErrorMessage('reCAPTCHA not ready. Please try again in a moment.');
            return;
        }

        const recaptchaToken = await executeRecaptcha('register');

        //validate
        updatePasswordCriteria(credentials.password); // Ensure checklist UI is updated on submit attempt
        if (!isPasswordStrong(credentials.password)) {
            setErrorMessage('Please ensure all password requirements are met (see checklist below).');
            return;
        }

        if(credentials.password !== credentials.passwordConfirmation) {
            console.log("password != passwordConfirmation");
            setErrorMessage('Password and password-confirmation are not equal.');
            return;
        }

        try {
            await postUser(credentials, recaptchaToken);
            setLoginValues({userName: credentials.email, password: credentials.password});
            setCredentials(initialState);
            navigate('/');
        } catch (error) {
            console.error('Failed to fetch to server:', error.message);
            setErrorMessage(error.message);
        }
    }, [executeRecaptcha, credentials, setLoginValues, navigate, setCredentials, initialState, updatePasswordCriteria]); // Added dependencies to useCallback

    return (
        <div>
            <h2>Register user</h2>
            <form onSubmit={handleSubmit}>
                <section>
                <aside>
                    <div>
                        <label>Firstname:</label>
                        <input
                            type="text"
                            value={credentials.firstName}
                            onChange={(e) =>
                                setCredentials(prevValues => ({...prevValues, firstName: e.target.value}))}
                            required
                            placeholder="Please enter your firstname *"
                        />
                    </div>
                    <div>
                        <label>Lastname:</label>
                        <input
                            type="text"
                            value={credentials.lastName}
                            onChange={(e) =>
                                setCredentials(prevValues => ({...prevValues, lastName: e.target.value}))}
                            required
                            placeholder="Please enter your lastname *"
                        />
                    </div>
                    <div>
                        <label>Email:</label>
                        <input
                            type="text"
                            value={credentials.email}
                            onChange={(e) =>
                                setCredentials(prevValues => ({...prevValues, email: e.target.value}))}
                            required
                            placeholder="Please enter your email"
                        />
                    </div>
                </aside>
                    <aside>
                        <div>
                            <label>Password:</label>
                            <input
                                type="password"
                                value={credentials.password}
                                onChange={(e) => {
                                    const newPassword = e.target.value;
                                    setCredentials(prevValues => ({...prevValues, password: newPassword}));
                                    updatePasswordCriteria(newPassword);
                                }}
                                required
                                placeholder="Please enter your pwd *"
                            />
                        </div>
                        <div>
                            <label>Password confirmation:</label>
                            <input
                                type="password"
                                value={credentials.passwordConfirmation}
                                onChange={(e) =>
                                    setCredentials(prevValues => ({...prevValues, passwordConfirmation: e.target.value}))}
                                required
                                placeholder="Please confirm your pwd *"
                            />
                        </div>
                        <div>
                            <ul style={{ listStyleType: 'none', paddingLeft: 0, marginTop: '10px', fontSize: '0.9em' }}>
                                <li style={{ color: passwordCriteriaStatus.minLength ? 'green' : '#666' }}>
                                    {passwordCriteriaStatus.minLength ? '✓' : '•'} At least 8 characters
                                </li>
                                <li style={{ color: passwordCriteriaStatus.hasUpperCase ? 'green' : '#666' }}>
                                    {passwordCriteriaStatus.hasUpperCase ? '✓' : '•'} At least one uppercase letter (A-Z)
                                </li>
                                <li style={{ color: passwordCriteriaStatus.hasLowerCase ? 'green' : '#666' }}>
                                    {passwordCriteriaStatus.hasLowerCase ? '✓' : '•'} At least one lowercase letter (a-z)
                                </li>
                                <li style={{ color: passwordCriteriaStatus.hasDigit ? 'green' : '#666' }}>
                                    {passwordCriteriaStatus.hasDigit ? '✓' : '•'} At least one digit (0-9)
                                </li>
                                <li style={{ color: passwordCriteriaStatus.hasSpecialChar ? 'green' : '#666' }}>
                                    {passwordCriteriaStatus.hasSpecialChar ? '✓' : '•'} At least one special character (e.g. !@#$%)
                                </li>
                            </ul>
                        </div>
                    </aside>
                </section>
                <button type="submit">Register</button>
                {errorMessage && <p style={{ color: 'red' }}>{errorMessage}</p>}
                <p style={{ fontSize: '0.8em', color: '#666', marginTop: '15px', textAlign: 'center' }}>
                    This site is protected by reCAPTCHA and the Google&nbsp;
                    <a href="https://policies.google.com/privacy" target="_blank" rel="noopener noreferrer">Privacy Policy</a> and&nbsp;
                    <a href="https://policies.google.com/terms" target="_blank" rel="noopener noreferrer">Terms of Service</a> apply.
                </p>
            </form>
        </div>
    );
}

export default RegisterUser;
