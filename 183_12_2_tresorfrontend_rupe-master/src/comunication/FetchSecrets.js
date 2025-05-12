/**
 * Fetch methodes for secret api calls
 * @author Peter Rutschmann
 */

//Post secret to server
export const postSecret = async ({userId, content}) => {
    const protocol = process.env.REACT_APP_API_PROTOCOL; // "http"
    const host = process.env.REACT_APP_API_HOST; // "localhost"
    const port = process.env.REACT_APP_API_PORT; // "8080"
    const path = process.env.REACT_APP_API_PATH; // "/api"
    const portPart = port ? `:${port}` : ''; // port is optional
    const API_URL = `${protocol}://${host}${portPart}${path}`;

    // We need to send the content as a raw JSON object, not as a JSON string
    // The backend expects the content field to be valid JSON
    try {
        const response = await fetch(`${API_URL}/secrets`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                userId: userId,
                content: JSON.stringify(content),
            })
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Server response failed.');
        }

        const data = await response.json();
        console.log('Secret successfully posted:', data);
        return data;
    } catch (error) {
        console.error('Error posting secret:', error.message);
        throw new Error('Failed to save secret. ' || error.message);
    }
};

//get all secrets for a user by userId
export const getSecretsforUser = async (userId) => {
    const protocol = process.env.REACT_APP_API_PROTOCOL; // "http"
    const host = process.env.REACT_APP_API_HOST; // "localhost"
    const port = process.env.REACT_APP_API_PORT; // "8080"
    const path = process.env.REACT_APP_API_PATH; // "/api"
    const portPart = port ? `:${port}` : ''; // port is optional
    const API_URL = `${protocol}://${host}${portPart}${path}`;

    try {
        const response = await fetch(`${API_URL}/secrets/user/${userId}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Server response failed.');
        }
        const data = await response.json();
        console.log('Secret successfully got:', data);
        return data;
    } catch (error) {
        console.error('Failed to get secrets:', error.message);
        throw new Error('Failed to get secrets. ' || error.message);
    }
};