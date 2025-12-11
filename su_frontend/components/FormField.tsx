import { ChangeEvent, useState } from 'react';
import { Eye, EyeOff } from 'lucide-react';

type FormFieldProps = {
    id: string;
    label: string;
    type: string;
    value: string;
    onChange: (e: ChangeEvent<HTMLInputElement>) => void;
    placeholder?: string;
    disabled: boolean;
    required?: boolean;
};

function FormField({
    id,
    label,
    type,
    value,
    onChange,
    placeholder,
    disabled,
    required = true,
}: FormFieldProps) {
    const [showPassword, setShowPassword] = useState(false);

    const isPasswordField = type === 'password';

    return (
        <div className="relative">
            <label
                htmlFor={id}
                className="text-txtcolor-300 mb-3 block text-xs tracking-wider uppercase"
            >
                {label}
            </label>
            <input
                id={id}
                type={isPasswordField ? (showPassword ? 'text' : 'password') : type}
                value={value}
                onChange={onChange}
                placeholder={placeholder}
                disabled={disabled}
                required={required}
                className="bg-inputbg placeholder-txtcolor-300 focus:ring-secondary max-h-[41px] w-full rounded-[22px] px-3 py-4 text-center focus:ring-2 focus:outline-none"
            />
            {isPasswordField && (
                <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="text-form-icon absolute inset-y-0 top-7 right-0 flex items-center pr-3"
                    aria-label={showPassword ? 'Ukryj hasło' : 'Pokaż hasło'}
                >
                    {showPassword ? <EyeOff size={20} /> : <Eye size={20} />}
                </button>
            )}
        </div>
    );
}

export default FormField;
