import React, { useEffect, useState } from 'react';

const API_BASE = 'https://localhost:443/users';

const UserProfiles = () => {
    const [users, setUsers] = useState([]);
    const [selectedUser, setSelectedUser] = useState(null);
    const [formData, setFormData] = useState({ id: '', username: '', email: '', ssn: '' });
    const [isEditing, setIsEditing] = useState(false);
    const [searchId, setSearchId] = useState('');
    const [searchError, setSearchError] = useState(null);

    useEffect(() => {
        fetchUsers();
    }, []);

    const fetchUsers = async () => {
        const res = await fetch(API_BASE);
        const data = await res.json();

        const mappedUsers = data.map(user => ({
            id: user.id,
            username: user.username,
            email: user.email,
            ssn: '', // SSN not returned by API in list
        }));

        setUsers(mappedUsers);
    };

    const fetchUser = async (id) => {
        const res = await fetch(`${API_BASE}/${id}`);
        const data = await res.json();
        setSelectedUser(data);
    };

    const handleDelete = async (id) => {
        await fetch(`${API_BASE}/${id}`, { method: 'DELETE' });
        fetchUsers();
    };

    const handleSearch = (e) => {
        e.preventDefault();
        if (!searchId) {
            alert('Please enter a user ID');
            return;
        }
        fetchUser(searchId);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        const method = isEditing ? 'PUT' : 'POST';
        const url = isEditing ? `${API_BASE}/${formData.id}` : API_BASE;

        const requestBody = {
            username: formData.username,
            email: formData.email,
        };

        if (!isEditing && formData.ssn) {
            requestBody.socialSecurityNumber = formData.ssn;
        }

        await fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(requestBody),
        });

        setFormData({ id: '', username: '', email: '', ssn: '' });
        setIsEditing(false);
        fetchUsers();
    };

    const handleEdit = (user) => {
        setFormData(user);
        setIsEditing(true);
    };

    return (
        <div className="max-w-3xl mx-auto p-4">
            <h1 className="text-xl font-bold mb-4">User Profiles</h1>

            {/* User table */}
            <table className="table-auto w-full border-collapse border border-gray-300 mb-6">
                <thead>
                    <tr>
                        <th className="border border-gray-300 px-4 py-2 text-left">Username</th>
                        <th className="border border-gray-300 px-4 py-2 text-left">Email</th>
                        <th className="border border-gray-300 px-4 py-2 text-left">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {users.map(user => (
                        <tr key={user.id}>
                            <td className="border border-gray-300 px-4 py-2">{user.username}</td>
                            <td className="border border-gray-300 px-4 py-2">{user.email}</td>
                            <td className="border border-gray-300 px-4 py-2">
                                <button
                                    onClick={() => fetchUser(user.id)}
                                    className="mr-2 text-blue-600 underline"
                                >
                                    View
                                </button>
                                <button
                                    onClick={() => handleEdit(user)}
                                    className="mr-2 text-green-600 underline"
                                >
                                    Edit
                                </button>
                                <button
                                    onClick={() => handleDelete(user.id)}
                                    className="text-red-600 underline"
                                >
                                    Delete
                                </button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>

            {/* Form */}
            <form onSubmit={handleSubmit} className="space-y-6 max-w-md mx-auto">
                <h2 className="text-xl font-semibold">{isEditing ? 'Edit User' : 'Create New User'}</h2>

                {/* Username */}
                <div className="flex gap-4">
                    <div className="w-32 text-r font-medium text-gray-700">
                        <label htmlFor="username" className="block">
                            Username
                        </label>
                    </div>
                    <input
                        id="username"
                        type="text"
                        value={formData.username}
                        onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                        required
                        className="border border-gray-300 rounded px-3 py-2 flex-grow"
                    />
                </div>

                {/* Email */}
                <div className="flex items-start gap-4">
                    <div className="w-32 text-left font-medium text-gray-700">
                        <label htmlFor="email" className="block">
                            Email
                        </label>
                    </div>
                    <input
                        id="email"
                        type="email"
                        value={formData.email}
                        onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                        required
                        className="border border-gray-300 rounded px-3 py-2 flex-grow"
                    />
                </div>

                {/* SSN */}
                {!isEditing && (
                    <div className="flex items-start gap-4">
                        <div className="w-32 text-left font-medium text-gray-700">
                            <label htmlFor="ssn" className="block">
                                SSN
                            </label>
                        </div>
                        <input
                            id="ssn"
                            type="text"
                            value={formData.ssn || ''}
                            onChange={(e) => setFormData({ ...formData, ssn: e.target.value })}
                            className="border border-gray-300 rounded px-3 py-2 flex-grow"
                        />
                    </div>
                )}

                {/* Submit Button */}
                <button
                    type="submit"
                    className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
                >
                    {isEditing ? 'Update User' : 'Create User'}
                </button>
            </form>

            {/* Search Form */}
            <form onSubmit={handleSearch} className="flex gap-2 mb-4">
                <h2 className="text-xl font-semibold">Search by ID</h2>
                <input
                    type="text"
                    value={searchId}
                    onChange={(e) => setSearchId(e.target.value)}
                    placeholder="Enter User ID"
                    className="border border-gray-300 rounded px-3 py-2"
                />
                <button
                    type="submit"
                    className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
                >
                    Search
                </button>
            </form>

            {selectedUser && (
                <div className="mt-6 p-4 border rounded bg-gray-100 shadow text-left">
                    <h2 className="text-lg font-semibold mb-2">Selected User Details</h2>
                    <p><strong>Username:</strong> {selectedUser.username}</p>
                    <p><strong>Email:</strong> {selectedUser.email}</p>
                    <p><strong>ID:</strong> {selectedUser.id}</p>
                    {selectedUser.ssn && (
                        <p><strong>SSN:</strong> {selectedUser.ssn}</p>
                    )}
                </div>
            )}
        </div>
    );
};

export default UserProfiles;
